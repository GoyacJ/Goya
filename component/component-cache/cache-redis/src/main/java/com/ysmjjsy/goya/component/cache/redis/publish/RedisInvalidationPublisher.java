package com.ysmjjsy.goya.component.cache.redis.publish;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.core.support.CacheKeySerializer;
import com.ysmjjsy.goya.component.cache.redis.constants.RedisConst;
import com.ysmjjsy.goya.component.cache.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>缓存失效消息发布器默认实现</p>
 * <p>基于 RedisService 的发布订阅功能实现缓存失效消息发布</p>
 *
 * @author goya
 * @see RedisService
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class RedisInvalidationPublisher {

    /**
     * Redis 服务
     */
    private final RedisService redisService;

    /**
     * 缓存键序列化器
     */
    private final CacheKeySerializer cacheKeySerializer;

    public <K> void publish(String cacheName, K key) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (key == null) {
            throw new IllegalArgumentException("Cache key cannot be null");
        }

        try {
            String serializedKey = serializeKey(key);
            RedisInvalidationMessage message = new RedisInvalidationMessage(cacheName, serializedKey);

            redisService.publish(RedisConst.CACHE_INVALIDATION_CHANNEL, message);

            if (log.isTraceEnabled()) {
                log.trace("Published cache invalidation message: cacheName={}, key={}", cacheName, key);
            }
        } catch (Exception e) {
            log.error("Failed to publish cache invalidation message: cacheName={}, key={}", cacheName, key, e);
            throw new CacheException("Failed to publish cache invalidation message", e);
        }
    }

    public <K> void publish(String cacheName, Iterable<K> keys) {
        if (cacheName == null) {
            throw new IllegalArgumentException("Cache name cannot be null");
        }
        if (keys == null) {
            throw new IllegalArgumentException("Cache keys cannot be null");
        }

        try {
            for (K key : keys) {
                publish(cacheName, key);
            }

            if (log.isDebugEnabled()) {
                log.debug("Published batch cache invalidation messages: cacheName={}, count={}", cacheName, count(keys));
            }
        } catch (Exception e) {
            log.error("Failed to publish batch cache invalidation messages: cacheName={}", cacheName, e);
            throw new CacheException("Failed to publish batch cache invalidation messages", e);
        }
    }

    /**
     * 序列化缓存键
     *
     * @param key 缓存键
     * @param <K> 键类型
     * @return 序列化后的字符串
     */
    private <K> String serializeKey(K key) {
        if (key instanceof String str) {
            return str;
        }
        return cacheKeySerializer.serializeToString(key);
    }

    /**
     * 计算迭代器中的元素数量
     *
     * @param iterable 迭代器
     * @param <K>      元素类型
     * @return 元素数量
     */
    private <K> int count(Iterable<K> iterable) {
        int count = 0;
        for (K ignored : iterable) {
            count++;
        }
        return count;
    }
}
