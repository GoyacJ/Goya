package com.ysmjjsy.goya.component.cache.redis.service;

import com.ysmjjsy.goya.component.cache.core.definition.CacheService;
import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.redis.configuration.properties.GoyaRedisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;

/**
 * <p>Redis 缓存服务实现</p>
 * <p>基于 Redisson 的 RBucket 实现 CacheService 接口，提供 Redis 作为 L2 远程缓存的实现</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 获取缓存值
 * String value = redisCacheService.get("userCache", "user:123");
 *
 * // 写入缓存（使用默认 TTL）
 * redisCacheService.put("userCache", "user:123", userInfo);
 *
 * // 删除缓存
 * redisCacheService.delete("userCache", "user:123");
 * }</pre>
 *
 * @author goya
 * @see CacheService
 * @see RedissonClient
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheService implements CacheService {

    private final RedissonClient redissonClient;
    private final CacheKeySerializer cacheKeySerializer;
    private final GoyaRedisProperties goyaRedisProperties;

    @Override
    public <K, V> V get(String cacheName, K key) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            String redisKey = buildRedisKey(cacheName, key);
            RBucket<V> bucket = redissonClient.getBucket(redisKey);
            V value = bucket.get();

            if (log.isTraceEnabled()) {
                log.trace("Get cache: cacheName={}, key={}, hit={}", cacheName, key, value != null);
            }

            return value;
        } catch (Exception e) {
            log.error("Failed to get cache: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to get cache", e);
        }
    }

    @Override
    public <K, V> void put(String cacheName, K key, V value) {
        put(cacheName, key, value, null);
    }

    /**
     * 写入缓存（指定 TTL）
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param value     缓存值
     * @param ttl       过期时间
     * @param <K>       键类型
     * @param <V>       值类型
     */
    public <K, V> void put(String cacheName, K key, V value, Duration ttl) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            String redisKey = buildRedisKey(cacheName, key);
            RBucket<V> bucket = redissonClient.getBucket(redisKey);

            if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
                bucket.set(value, ttl);
            } else {
                bucket.set(value);
            }

            if (log.isTraceEnabled()) {
                log.trace("Put cache: cacheName={}, key={}, ttl={}", cacheName, key, ttl);
            }
        } catch (Exception e) {
            log.error("Failed to put cache: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to put cache", e);
        }
    }

    @Override
    public <K> void delete(String cacheName, K key) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            String redisKey = buildRedisKey(cacheName, key);
            RBucket<Object> bucket = redissonClient.getBucket(redisKey);
            boolean deleted = bucket.delete();

            if (log.isTraceEnabled()) {
                log.trace("Delete cache: cacheName={}, key={}, deleted={}", cacheName, key, deleted);
            }
        } catch (Exception e) {
            log.error("Failed to delete cache: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to delete cache", e);
        }
    }

    @Override
    public <K> boolean exists(String cacheName, K key) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            String redisKey = buildRedisKey(cacheName, key);
            RBucket<Object> bucket = redissonClient.getBucket(redisKey);
            boolean exists = bucket.isExists();

            if (log.isTraceEnabled()) {
                log.trace("Exists cache: cacheName={}, key={}, exists={}", cacheName, key, exists);
            }

            return exists;
        } catch (Exception e) {
            log.error("Failed to check cache exists: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to check cache exists", e);
        }
    }

    /**
     * 构建 Redis Key
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @return Redis Key
     */
    private <K> String buildRedisKey(String cacheName, K key) {
        return cacheKeySerializer.buildKey(
                goyaRedisProperties.keyPrefix(),
                cacheName,
                key
        );
    }
}
