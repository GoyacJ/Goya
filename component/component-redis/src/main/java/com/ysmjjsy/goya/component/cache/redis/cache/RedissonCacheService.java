package com.ysmjjsy.goya.component.cache.redis.cache;

import com.ysmjjsy.goya.component.cache.redis.autoconfigure.properties.GoyaRedisProperties;
import com.ysmjjsy.goya.component.cache.redis.support.RedisKeyBuilder;
import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/25 21:52
 */
@RequiredArgsConstructor
public class RedissonCacheService implements CacheService {

    private final RedissonClient redisson;
    private final GoyaRedisProperties props;
    private final RedisKeyBuilder keyBuilder;

    @Override
    public <T> T get(String cacheName, Object key, Class<T> type) {
        Assert.hasText(cacheName, "cacheName 不能为空");
        Assert.notNull(key, "key 不能为空");

        Object v = map(cacheName).get(key);
        if (v == null || v == NullValue.INSTANCE) {
            return null;
        }
        if (type == null) {
            @SuppressWarnings("unchecked")
            T t = (T) v;
            return t;
        }
        if (!type.isInstance(v)) {
            throw new IllegalStateException("缓存类型不匹配，cacheName=" + cacheName + " key=" + key
                    + "，期望=" + type.getName() + "，实际=" + v.getClass().getName());
        }
        return type.cast(v);
    }

    @Override
    public <T> Optional<T> getOptional(String cacheName, Object key, Class<T> type) {
        return Optional.ofNullable(get(cacheName, key, type));
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        put(cacheName, key, value, props.defaultTtl());
    }

    @Override
    public void put(String cacheName, Object key, Object value, Duration ttl) {
        Assert.hasText(cacheName, "cacheName 不能为空");
        Assert.notNull(key, "key 不能为空");

        if (value == null && !props.allowNullValues()) {
            evict(cacheName, key);
            return;
        }

        Object store = (value == null) ? NullValue.INSTANCE : value;
        Duration useTtl = (ttl == null) ? props.defaultTtl() : ttl;

        // ttl=0：立即过期，直接删除
        if (useTtl != null && useTtl.isZero()) {
            evict(cacheName, key);
            return;
        }

        // ttl<0：不过期（永久）
        if (useTtl != null && useTtl.isNegative()) {
            map(cacheName).put(key, store);
            return;
        }

        // ttl>0：按条目 TTL 写入
        long ms = (useTtl == null) ? props.defaultTtl().toMillis() : useTtl.toMillis();
        if (ms <= 0) {
            evict(cacheName, key);
            return;
        }
        map(cacheName).put(key, store, ms, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean evict(String cacheName, Object key) {
        Assert.hasText(cacheName, "cacheName 不能为空");
        Assert.notNull(key, "key 不能为空");
        return map(cacheName).fastRemove(key) > 0;
    }

    @Override
    public void clear(String cacheName) {
        Assert.hasText(cacheName, "cacheName 不能为空");
        map(cacheName).clear();
    }

    @Override
    public Map<Object, Object> getAll(String cacheName, Collection<?> keys) {
        Assert.hasText(cacheName, "cacheName 不能为空");
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }
        Map<Object, Object> found = map(cacheName).getAll(new HashSet<>(keys));
        if (found == null || found.isEmpty()) {
            return Map.of();
        }
        Map<Object, Object> out = LinkedHashMap.newLinkedHashMap(found.size());
        for (Map.Entry<Object, Object> e : found.entrySet()) {
            Object v = e.getValue();
            if (v == null || v == NullValue.INSTANCE) {
                continue;
            }
            out.put(e.getKey(), v);
        }
        return out;
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        return getOrLoad(cacheName, key, type, null, loader);
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Assert.hasText(cacheName, "cacheName 不能为空");
        Assert.notNull(key, "key 不能为空");
        Objects.requireNonNull(loader, "loader 不能为空");

        // 先读缓存
        T existed = get(cacheName, key, type);
        if (existed != null) {
            return existed;
        }

        // 未开启防击穿：直接加载写入
        if (!props.stampedeLockEnabled()) {
            T loaded = loader.get();
            put(cacheName, key, loaded, ttl);
            return loaded;
        }

        String lockKey = keyBuilder.stampedeLockKey(cacheName, key);
        RLock lock = redisson.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(props.stampedeLockWait().toMillis(),
                    props.stampedeLockLease().toMillis(),
                    TimeUnit.MILLISECONDS);

            // 拿不到锁：轻量重试一次读取，降低击穿
            if (!locked) {
                T retry = get(cacheName, key, type);
                if (retry != null) {
                    return retry;
                }
                // 兜底：不阻塞，直接加载
                T loaded = loader.get();
                put(cacheName, key, loaded, ttl);
                return loaded;
            }

            // 拿锁后二次检查
            T again = get(cacheName, key, type);
            if (again != null) {
                return again;
            }

            T loaded = loader.get();
            put(cacheName, key, loaded, ttl);
            return loaded;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            T loaded = loader.get();
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

    private RMapCache<Object, Object> map(String cacheName) {
        String mapName = keyBuilder.cacheMapName(cacheName);
        return redisson.getMapCache(mapName);
    }


    /**
     * Null 值标记：避免 Redis 存储 null。
     */
    private enum NullValue {
        /** 单例 */
        INSTANCE
    }
}