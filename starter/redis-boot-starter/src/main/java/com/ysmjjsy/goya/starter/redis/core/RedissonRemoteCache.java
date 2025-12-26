package com.ysmjjsy.goya.starter.redis.core;

import com.ysmjjsy.goya.component.cache.core.GoyaCache;
import com.ysmjjsy.goya.component.cache.core.RemoteCache;
import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.serializer.CacheKeySerializer;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.redisson.api.RBatch;
import org.redisson.api.RBucket;
import org.redisson.api.RBucketAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.springframework.cache.support.SimpleValueWrapper;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 远程缓存实现
 *
 * <p>使用 Redisson 实现 {@link RemoteCache} 接口，提供 Redis 远程缓存能力。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>使用 RedissonClient 操作 Redis</li>
 *   <li>支持 TTL 配置的写入操作</li>
 *   <li>支持批量操作以提升性能</li>
 *   <li>处理 NullValueWrapper 的序列化</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>实现 {@link RemoteCache} 接口，由 {@link GoyaCache} 使用</li>
 *   <li>返回的 ValueWrapper 完全兼容 Spring Cache SPI</li>
 * </ul>
 *
 * <p><b>执行流程：</b>
 * <ol>
 *   <li><b>get(key)：</b>
 *     <ol>
 *       <li>构建 Redis key（包含 cacheName 前缀）</li>
 *       <li>使用 RBucket.get() 获取值</li>
 *       <li>反序列化并返回 ValueWrapper</li>
 *     </ol>
 *   </li>
 *   <li><b>put(key, value, ttl)：</b>
 *     <ol>
 *       <li>构建 Redis key</li>
 *       <li>序列化 value（Redisson 自动处理）</li>
 *       <li>使用 RBucket.set() 写入，指定 TTL</li>
 *     </ol>
 *   </li>
 * </ol>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>Redisson 客户端是线程安全的，支持高并发操作</li>
 *   <li>所有操作都是同步的（除非使用异步方法）</li>
 * </ul>
 *
 * <p><b>异常处理：</b>
 * <ul>
 *   <li>如果 key 为 null，抛出 {@link IllegalArgumentException}</li>
 *   <li>如果 TTL 无效，抛出 {@link IllegalArgumentException}</li>
 *   <li>如果网络异常，抛出 {@link RuntimeException}（包装 Redisson 异常）</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:59
 */
@Slf4j
public class RedissonRemoteCache implements RemoteCache {

    /**
     * 缓存名称
     */
    private final String name;

    /**
     * Redisson 客户端
     */
    private final RedissonClient redisson;

    /**
     * 编解码器（用于序列化/反序列化）
     */
    private final Codec codec;

    /**
     * 缓存键序列化器
     */
    private final CacheKeySerializer keySerializer;

    /**
     * Redis key 前缀（从配置获取，默认值："cache:"）
     */
    private final String keyPrefix;

    /**
     * 缓存配置规范
     */
    private final CacheSpecification spec;

    /**
     * 构造函数
     *
     * @param name 缓存名称
     * @param redisson Redisson 客户端
     * @param codec 编解码器（可选）
     * @param spec 缓存配置规范
     * @param keySerializer 缓存键序列化器（可选）
     */
    public RedissonRemoteCache(String name, RedissonClient redisson, Codec codec,
                               CacheSpecification spec, CacheKeySerializer keySerializer) {
        this.name = name;
        this.redisson = redisson;
        this.codec = codec;
        this.spec = spec;
        this.keySerializer = keySerializer;
        this.keyPrefix = spec.getKeyPrefix();
    }

    @Override
    @NullMarked
    public String getName() {
        return name;
    }

    @Override
    @NullMarked
    public Object getNativeCache() {
        return redisson;
    }

    @Override
    public ValueWrapper get(@NonNull Object key) {
        String redisKey = buildKey(key);
        RBucket<Object> bucket = codec != null
                ? redisson.getBucket(redisKey, codec)
                : redisson.getBucket(redisKey);

        Object value = bucket.get();
        return value != null ? new SimpleValueWrapper(value) : null;
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        if (wrapper == null) {
            return null;
        }
        Object value = wrapper.get();
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            @SuppressWarnings("unchecked")
            T value = (T) wrapper.get();
            return value;
        }

        T value;
        try {
            value = valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }

        // 写入缓存（使用配置的 TTL）
        put(key, value, spec.getTtl());
        return value;
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        // 使用配置的 TTL
        put(key, value, spec.getTtl());
    }

    @Override
    public void put(Object key, Object value, Duration ttl) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        String redisKey = buildKey(key);
        RBucket<Object> bucket = codec != null
                ? redisson.getBucket(redisKey, codec)
                : redisson.getBucket(redisKey);

        // 设置值并指定 TTL
        bucket.set(value, ttl.toMillis(), TimeUnit.MILLISECONDS);

        if (log.isTraceEnabled()) {
            log.trace("Put to RedissonRemoteCache: name={}, key={}, ttl={}", name, key, ttl);
        }
    }

    @Override
    public void evict(@NonNull Object key) {
        String redisKey = buildKey(key);
        RBucket<Object> bucket = codec != null
                ? redisson.getBucket(redisKey, codec)
                : redisson.getBucket(redisKey);

        bucket.delete();
    }

    @Override
    public void clear() {
        // 删除所有以该 cacheName 为前缀的 key
        // 注意：这是一个昂贵的操作，应该谨慎使用
        String pattern = keyPrefix + name + ":*";
        redisson.getKeys().deleteByPattern(pattern);

        log.info("Cleared RedissonRemoteCache: name={}", name);
    }

    @Override
    public Map<Object, ValueWrapper> getAll(Set<Object> keys) {
        if (keys == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        if (keys.isEmpty()) {
            return new HashMap<>();
        }

        Map<Object, ValueWrapper> result = new HashMap<>();
        for (Object key : keys) {
            if (key == null) {
                continue; // 跳过 null key
            }
            ValueWrapper wrapper = get(key);
            if (wrapper != null) {
                result.put(key, wrapper);
            }
        }
        return result;
    }

    @Override
    public void putAll(Map<Object, Object> entries, Duration ttl) {
        if (entries == null) {
            throw new IllegalArgumentException("Entries cannot be null");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("TTL must be positive, got: " + ttl);
        }

        if (entries.isEmpty()) {
            return;
        }

        // 使用 Redisson 的 RBatch 实现批量操作（Pipeline）
        // 所有写入操作使用异步 API，批量执行后等待完成
        RBatch batch = redisson.createBatch();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null) {
                continue; // 跳过 null key
            }

            String redisKey = buildKey(key);
            RBucketAsync<Object> bucketAsync = codec != null
                    ? batch.getBucket(redisKey, codec)
                    : batch.getBucket(redisKey);

            // 使用异步 API 设置值并指定 TTL
            bucketAsync.setAsync(value, ttl.toMillis(), TimeUnit.MILLISECONDS);
        }

        // 批量执行
        try {
            batch.execute();
            if (log.isTraceEnabled()) {
                log.trace("Batch put to RedissonRemoteCache: name={}, count={}, ttl={}",
                        name, entries.size(), ttl);
            }
        } catch (Exception e) {
            log.error("Failed to batch put to RedissonRemoteCache: name={}, count={}",
                    name, entries.size(), e);
            // 批量操作失败，降级到逐个写入
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key == null) {
                    continue;
                }
                try {
                    put(key, value, ttl);
                } catch (Exception ex) {
                    log.warn("Failed to put entry to RedissonRemoteCache during fallback: key={}", key, ex);
                }
            }
        }
    }

    @Override
    public CompletableFuture<ValueWrapper> getAsync(Object key) {
        if (key == null) {
            return CompletableFuture.completedFuture(null);
        }

        String redisKey = buildKey(key);
        RBucket<Object> bucket = codec != null
                ? redisson.getBucket(redisKey, codec)
                : redisson.getBucket(redisKey);

        return bucket.getAsync()
                .thenApply(value ->
                        value != null
                                ? (ValueWrapper) new SimpleValueWrapper(value)
                                : null
                )
                .toCompletableFuture();
    }

    /**
     * 构建 Redis key
     *
     * <p>使用 CacheKeySerializer 序列化 key，然后转换为字符串。
     * 对于简单类型（String、Long），直接使用字符串拼接。
     *
     * @param key 缓存键
     * @return Redis key（包含 cacheName 前缀）
     */
    private String buildKey(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        // 优化：常见类型直接使用字符串拼接
        if (key instanceof String) {
            return keyPrefix + name + ":" + key;
        }

        if (key instanceof Long || key instanceof Integer) {
            return keyPrefix + name + ":" + key.toString();
        }

        // 其他类型：使用序列化器序列化后转换为 Base64 字符串
        if (keySerializer != null) {
            byte[] keyBytes = keySerializer.serialize(key);
            String serializedKey = Base64.getEncoder().encodeToString(keyBytes);
            return keyPrefix + name + ":" + serializedKey;
        } else {
            // 如果没有序列化器，使用 toString()
            return keyPrefix + name + ":" + key.toString();
        }
    }

    // ValueRetrievalException 使用 Spring Cache 的标准异常
}

