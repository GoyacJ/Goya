package com.ysmjjsy.goya.component.cache.multilevel.publish;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.redis.publish.RedisInvalidationMessage;
import com.ysmjjsy.goya.component.cache.redis.publish.RedisInvalidationMessageListener;
import com.ysmjjsy.goya.component.cache.redis.publish.RedisInvalidationSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * <p>Redis 缓存失效消息订阅器适配器</p>
 * <p>将 cache-redis 的 RedisInvalidationSubscriber 适配为 cache-multi-level 的 CacheInvalidationSubscriber 接口</p>
 * <p>与 RedisCacheInvalidationPublisher 保持一致的适配方式</p>
 *
 * @author goya
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheInvalidationSubscriber implements CacheInvalidationSubscriber {

    /**
     * Redis 缓存失效消息订阅器（cache-redis 模块的实现）
     */
    private final RedisInvalidationSubscriber redisSubscriber;

    @Override
    public void subscribe(CacheInvalidationMessageListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Message listener cannot be null");
        }

        try {
            // 将 cache-multi-level 的 CacheInvalidationMessageListener 适配为
            // cache-redis 的 RedisInvalidationMessageListener
            RedisInvalidationMessageListener redisListener = message -> {
                if (message != null && message.isValid()) {
                    // 将 cache-redis 的 RedisInvalidationMessage 适配为
                    // cache-multi-level 的 CacheInvalidationMessage
                    CacheInvalidationMessage adaptedMessage = adaptMessage(message);
                    listener.onMessage(adaptedMessage);
                }
            };

            redisSubscriber.subscribe(redisListener);

            if (log.isDebugEnabled()) {
                log.debug("Subscribed to cache invalidation messages via adapter");
            }
        } catch (Exception e) {
            log.error("Failed to subscribe to cache invalidation messages via adapter", e);
            throw new CacheException("Failed to subscribe to cache invalidation messages", e);
        }
    }

    @Override
    public void unsubscribe() {
        try {
            redisSubscriber.unsubscribe();
            if (log.isDebugEnabled()) {
                log.debug("Unsubscribed from cache invalidation messages via adapter");
            }
        } catch (Exception e) {
            log.error("Failed to unsubscribe from cache invalidation messages via adapter", e);
            throw new CacheException("Failed to unsubscribe from cache invalidation messages", e);
        }
    }

    /**
     * 将 cache-redis 的 RedisInvalidationMessage 适配为 cache-multi-level 的 CacheInvalidationMessage
     *
     * @param redisMessage cache-redis 的消息
     * @return cache-multi-level 的消息格式
     */
    private CacheInvalidationMessage adaptMessage(RedisInvalidationMessage redisMessage) {
        return new CacheInvalidationMessage() {
            @Serial
            private static final long serialVersionUID = -5141991944358215288L;

            @Override
            public String cacheName() {
                return redisMessage.cacheName();
            }

            @Override
            public String key() {
                return redisMessage.key();
            }
        };
    }
}
