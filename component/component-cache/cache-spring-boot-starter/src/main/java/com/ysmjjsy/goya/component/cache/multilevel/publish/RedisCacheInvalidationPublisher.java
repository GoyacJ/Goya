package com.ysmjjsy.goya.component.cache.multilevel.publish;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.redis.publish.RedisInvalidationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Redis 缓存失效消息发布器适配器</p>
 * <p>将 cache-redis 的 RedisCacheInvalidationPublisher 适配为 cache-multi-level 的 CacheInvalidationPublisher 接口</p>
 * <p>与 RedisCacheInvalidationSubscriber 保持一致的适配方式</p>
 *
 * @author goya
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheInvalidationPublisher implements CacheInvalidationPublisher {

    /**
     * Redis 缓存失效消息发布器（cache-redis 模块的实现）
     */
    private final RedisInvalidationPublisher redisPublisher;

    @Override
    public <K> void publish(String cacheName, K key) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            redisPublisher.publish(cacheName, key);
            if (log.isTraceEnabled()) {
                log.trace("Published cache invalidation message via adapter: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to publish cache invalidation message via adapter: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to publish cache invalidation message", e);
        }
    }

    @Override
    public <K> void publish(String cacheName, Iterable<K> keys) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (keys == null) {
            throw new IllegalArgumentException("Cache keys cannot be null");
        }

        try {
            redisPublisher.publish(cacheName, keys);
            if (log.isTraceEnabled()) {
                log.trace("Published batch cache invalidation messages via adapter: cacheName={}", cacheName);
            }
        } catch (Exception e) {
            log.error("Failed to publish batch cache invalidation messages via adapter: cacheName={}", cacheName, e);
            throw new CacheException("Failed to publish batch cache invalidation messages", e);
        }
    }
}
