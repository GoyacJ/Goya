package com.ysmjjsy.goya.component.cache.redis.cache;

import com.ysmjjsy.goya.component.cache.redis.autoconfigure.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.cache.key.CacheKeySerializer;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.redisson.codec.TypedJsonJackson3Codec;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * <p>基于 Redisson 的远程缓存实现（L2，严格模式）</p>
 * <p><b>Key 策略：</b></p>
 * <ul>
 *   <li>最终 Redis Key 为 String</li>
 *   <li>统一由 {@link CacheKeySerializer#buildKey(String, String, Object)} 生成</li>
 *   <li>支持“逻辑清空”：通过 cacheName 版本号实现（避免 scan 删除）</li>
 * </ul>
 *
 * <p><b>Value 策略：</b></p>
 * <ul>
 *   <li>依赖全局 {@code TypedJsonMapperCodec}（携带 typeId）恢复真实类型</li>
 *   <li>本类不做 JsonMapper 二次转换，仅做类型断言（fail-fast）</li>
 * </ul>
 *
 * <p><b>clear(cacheName)：</b></p>
 * <ul>
 *   <li>版本号 +1，旧 key 自然不可达</li>
 *   <li>不执行 scan/delete，避免性能与误删风险</li>
 * </ul>
 *
 * <p><b>getOrLoad 防击穿：</b></p>
 * <ul>
 *   <li>可选使用分布式锁（RLock）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/25 21:52
 */
@RequiredArgsConstructor
public class RedissonCacheService implements CacheService {

    /**
     * 版本号 key 的命名空间（避免与业务缓存冲突）。
     */
    private static final String VERSION_NAMESPACE = "__cachever__";

    private final RedissonClient redisson;
    private final GoyaRedisProperties props;
    private final CacheKeySerializer cacheKeySerializer;

    @Override
    public <K, V> V get(String cacheName, K key, Class<V> type) {
        String redisKey = buildRedisKey(cacheName, key);
        Codec codec = new TypedJsonJackson3Codec(type);
        RBucket<V> bucket = bucket(redisKey, codec);
        return castOrFail(bucket.get(), type);
    }

    @Override
    public <K, V> Optional<V> getOptional(String cacheName, K key) {
        return Optional.ofNullable(get(cacheName, key, null));
    }

    @Override
    public <K, V> Optional<V> getOptional(String cacheName, K key, Class<V> type) {
        return Optional.ofNullable(get(cacheName, key, type));
    }

    @Override
    public <K, V> void put(String cacheName, K key, V value) {
        put(cacheName, key, value, props.defaultTtl());
    }

    @Override
    public <K, V> void put(String cacheName, K key, V value, Duration ttl) {
        if (value == null && !props.allowNullValues()) {
            delete(cacheName, key);
            return;
        }

        Duration useTtl = (ttl == null) ? props.defaultTtl() : ttl;

        // ttl=0：立即过期 -> 删除
        if (useTtl.isZero()) {
            delete(cacheName, key);
            return;
        }

        String redisKey = buildRedisKey(cacheName, key);

        // ttl<0：永久
        if (useTtl.isNegative()) {
            bucket(redisKey, null).set(value);
            return;
        }

        long ms = useTtl.toMillis();
        if (ms <= 0) {
            delete(cacheName, key);
            return;
        }
        bucket(redisKey, null).set(value, useTtl);
    }

    @Override
    public <K> boolean delete(String cacheName, K key) {
        String redisKey = buildRedisKey(cacheName, key);
        return bucket(redisKey, null).delete();
    }

    @Override
    public void clear(String cacheName) {
        versionCounter(cacheName).incrementAndGet();
    }

    @Override
    public <K> boolean exists(String cacheName, K key) {
        String buildRedisKey = buildRedisKey(cacheName, key);
        return redisson.getBucket(buildRedisKey).isExists();
    }

    @Override
    public <K, V> Map<K, V> getAll(String cacheName, Collection<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<K, String> keyToRedisKey = new LinkedHashMap<>();
        for (K k : keys) {
            if (k == null) {
                continue;
            }
            keyToRedisKey.put(k, buildRedisKey(cacheName, k));
        }
        if (keyToRedisKey.isEmpty()) {
            return Map.of();
        }

        // RBatch pipeline：一次性发送多个 GET
        RBatch batch = redisson.createBatch();
        Map<K, RFuture<V>> futures = LinkedHashMap.newLinkedHashMap(keyToRedisKey.size());

        for (Map.Entry<K, String> e : keyToRedisKey.entrySet()) {
            RBucketAsync<V> bucket = batch.getBucket(e.getValue());
            RFuture<V> f = bucket.getAsync();
            futures.put(e.getKey(), f);
        }

        batch.execute();

        Map<K, V> out = LinkedHashMap.newLinkedHashMap(futures.size());
        for (Map.Entry<K, RFuture<V>> e : futures.entrySet()) {
            try {
                V raw = e.getValue().toCompletableFuture().join();
                if (raw != null) {
                    out.put(e.getKey(), raw);
                }
            } catch (Exception _) {
                // 单个 key 失败不影响整体：跳过
            }
        }
        return out;
    }

    @Override
    public <K, V> V getOrLoad(String cacheName, K key, Supplier<V> loader) {
        return getOrLoad(cacheName, key, null, null, loader);
    }

    @Override
    public <K, V> V getOrLoad(String cacheName, K key, Class<V> type, Supplier<V> loader) {
        return getOrLoad(cacheName, key, type, null, loader);
    }

    @Override
    public <K, V> V getOrLoad(String cacheName, K key, Duration ttl, Supplier<V> loader) {
        return getOrLoad(cacheName, key, null, ttl, loader);
    }

    @Override
    public <K, V> V getOrLoad(String cacheName, K key, Class<V> type, Duration ttl, Supplier<V> loader) {
        V existed = get(cacheName, key, type);
        if (existed != null) {
            return existed;
        }

        if (!props.stampedeLockEnabled()) {
            V loaded = loader.get();
            put(cacheName, key, loaded, ttl);
            return loaded;
        }

        String redisKey = buildRedisKey(cacheName, key);
        String lockKey = redisKey + ":lock";
        RLock lock = redisson.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(props.stampedeLockWait().toMillis(),
                    props.stampedeLockLease().toMillis(),
                    TimeUnit.MILLISECONDS);

            if (!locked) {
                // 拿不到锁：轻量重试一次
                V retry = get(cacheName, key, type);
                if (retry != null) {
                    return retry;
                }
                V loaded = loader.get();
                put(cacheName, key, loaded, ttl);
                return loaded;
            }

            // 二次检查
            V again = get(cacheName, key, type);
            if (again != null) {
                return again;
            }

            V loaded = loader.get();
            put(cacheName, key, loaded, ttl);
            return loaded;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            V loaded = loader.get();
            put(cacheName, key, loaded, ttl);
            return loaded;
        } finally {
            if (locked) {
                try {
                    lock.unlock();
                } catch (Exception _) {
                    // ignore
                }
            }
        }
    }

    @Override
    public <K, V> boolean putIfAbsent(String cacheName, K key, V value, Duration expire) {
        String buildRedisKey = buildRedisKey(cacheName, key);
        RBucket<V> bucket = redisson.getBucket(buildRedisKey);
        if (expire == null || expire.isZero() || expire.isNegative()) {
            // 不带 TTL 的 NX
            return bucket.setIfAbsent(value);
        }
        return bucket.setIfAbsent(value, expire);
    }

    @Override
    public <K> long incrByWithTtlOnCreate(String cacheName, K key, long delta, Duration ttlOnCreate) {
        String buildRedisKey = buildRedisKey(cacheName, key);
        long ttlSec = (ttlOnCreate == null ? 0L : Math.max(0L, ttlOnCreate.toSeconds()));

        // 原子逻辑：
        // exists = EXISTS key
        // v = INCRBY key delta
        // if exists == 0 and ttlSec > 0 then EXPIRE key ttlSec end
        // return v
        String lua =
                "local exists = redis.call('EXISTS', KEYS[1]);" +
                        "local v = redis.call('INCRBY', KEYS[1], ARGV[1]);" +
                        "if (exists == 0 and tonumber(ARGV[2]) > 0) then redis.call('EXPIRE', KEYS[1], ARGV[2]); end;" +
                        "return v;";

        Number r = redisson.getScript().eval(
                RScript.Mode.READ_WRITE,
                lua,
                RScript.ReturnType.LONG,
                Collections.singletonList(buildRedisKey),
                String.valueOf(delta),
                String.valueOf(ttlSec)
        );
        return r.longValue();
    }

    @Override
    public <K> Long getCounter(String cacheName, K key) {
        String buildRedisKey = buildRedisKey(cacheName, key);
        RBucket<Object> bucket = redisson.getBucket(buildRedisKey);
        Object v = bucket.get();
        if (v == null) {
            return null;
        }
        if (v instanceof Number n) {
            return n.longValue();
        }
        // 兼容 Redis string 数值
        return Long.valueOf(String.valueOf(v));
    }

    @Override
    public <K> void resetCounter(String cacheName, K key) {
        delete(cacheName, key);
    }

    /**
     * 获取 RBucket（依赖 Redisson 全局 Codec 做序列化/反序列化）。
     *
     * @param redisKey Redis key
     * @return bucket
     */
    private <V> RBucket<V> bucket(String redisKey, Codec codec) {
        if (Objects.isNull(codec)) {
            return redisson.getBucket(redisKey);
        }
        return redisson.getBucket(redisKey, codec);
    }

    /**
     * 构建最终 Redis key（包含 cacheName 版本号）。
     *
     * @param cacheName 缓存名
     * @param key       业务 key
     * @return Redis key
     */
    private String buildRedisKey(String cacheName, Object key) {
        long ver = currentVersion(cacheName);
        String effectiveCacheName = cacheName + ":v" + ver;

        String redisKey = cacheKeySerializer.buildKey(props.keyPrefix(), effectiveCacheName, key);
        if (redisKey == null || redisKey.isBlank()) {
            throw new IllegalStateException("buildKey 结果为空，cacheName=" + cacheName + " keyType=" + key.getClass().getName());
        }
        return redisKey;
    }

    /**
     * 获取当前版本号。
     *
     * @param cacheName 缓存名
     * @return 版本号（默认 1）
     */
    private long currentVersion(String cacheName) {
        long v = versionCounter(cacheName).get();
        return (v <= 0) ? 1L : v;
    }

    /**
     * 获取版本号计数器。
     *
     * @param cacheName 缓存名
     * @return 原子计数器
     */
    private RAtomicLong versionCounter(String cacheName) {
        String versionKey = cacheKeySerializer.buildKey(props.keyPrefix(), VERSION_NAMESPACE, cacheName);
        if (versionKey == null || versionKey.isBlank()) {
            throw new IllegalStateException("版本号 key 构建失败，cacheName=" + cacheName);
        }
        return redisson.getAtomicLong(versionKey);
    }

    /**
     * 严格类型断言：解码结果必须是目标类型，否则 fail-fast。
     *
     * <p>由于 {@code TypedJsonMapperCodec} 已携带 typeId 并恢复真实类型，正常情况下应当匹配。</p>
     *
     * @param raw  原始值
     * @param type 目标类型
     * @param <T>  泛型
     * @return 目标类型对象
     */
    @SuppressWarnings("all")
    private <V> V castOrFail(V raw, Class<V> type) {
        if (raw == null) {
            return null;
        }
        if (type == null) {
            return (V) raw;
        }
        if (type.isInstance(raw)) {
            return type.cast(raw);
        }

        throw new IllegalStateException("缓存值类型不匹配，rawType=" + raw.getClass().getName()
                + " targetType=" + type.getName());
    }
}