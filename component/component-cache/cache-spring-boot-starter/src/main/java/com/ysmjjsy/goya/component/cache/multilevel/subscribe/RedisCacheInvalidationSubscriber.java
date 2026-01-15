package com.ysmjjsy.goya.component.cache.multilevel.subscribe;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationMessage;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationMessageListener;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationSubscriber;
import com.ysmjjsy.goya.component.cache.redis.constants.RedisConst;
import com.ysmjjsy.goya.component.cache.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serial;

/**
 * <p>Redis 缓存失效消息订阅器</p>
 * <p>基于 RedisService 实现缓存失效消息订阅</p>
 * <p>将 cache-redis 的 CacheInvalidationMessage 适配为 cache-multi-level 的消息格式</p>
 *
 * @author goya
 * @since 2026/1/15
 */
@Slf4j
@RequiredArgsConstructor
public class RedisCacheInvalidationSubscriber implements CacheInvalidationSubscriber {

    /**
     * Redis 服务
     */
    private final RedisService redisService;

    /**
     * 订阅 ID
     */
    private Integer subscriptionId;

    /**
     * 消息监听器
     */
    private CacheInvalidationMessageListener listener;

    /**
     * 是否已订阅
     */
    private boolean subscribed = false;

    @Override
    public void subscribe(CacheInvalidationMessageListener listener) {
        if (subscribed) {
            throw new IllegalStateException("Already subscribed to cache invalidation messages");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Message listener cannot be null");
        }

        this.listener = listener;

        try {
            // 使用 RedisService 订阅失效消息
            // 注意：使用完全限定名避免与 cache-multi-level 的 CacheInvalidationMessage 冲突
            subscriptionId = redisService.subscribe(
                    RedisConst.CACHE_INVALIDATION_CHANNEL,
                    com.ysmjjsy.goya.component.cache.redis.publish.CacheInvalidationMessage.class,
                    (_, message) -> {
                        if (message != null && message.isValid()) {
                            // 将 cache-redis 的消息适配为 cache-multi-level 的消息格式
                            CacheInvalidationMessage adaptedMessage = adaptMessage(message);
                            listener.onMessage(adaptedMessage);
                        }
                    }
            );

            subscribed = true;
            log.debug("Subscribed to cache invalidation channel: channel={}, subscriptionId={}",
                    RedisConst.CACHE_INVALIDATION_CHANNEL, subscriptionId);
        } catch (Exception e) {
            log.error("Failed to subscribe to cache invalidation channel: channel={}",
                    RedisConst.CACHE_INVALIDATION_CHANNEL, e);
            throw new CacheException("Failed to subscribe to cache invalidation channel", e);
        }
    }

    @Override
    public void unsubscribe() {
        if (!subscribed || subscriptionId == null) {
            return;
        }

        try {
            redisService.unsubscribe(RedisConst.CACHE_INVALIDATION_CHANNEL, subscriptionId);
            subscribed = false;
            subscriptionId = null;
            listener = null;
            log.debug("Unsubscribed from cache invalidation channel: subscriptionId={}", subscriptionId);
        } catch (Exception e) {
            log.error("Failed to unsubscribe from cache invalidation channel: subscriptionId={}", subscriptionId, e);
            throw new CacheException("Failed to unsubscribe from cache invalidation channel", e);
        }
    }

    /**
     * 将 cache-redis 的消息适配为 cache-multi-level 的消息格式
     *
     * @param redisMessage cache-redis 的消息
     * @return cache-multi-level 的消息格式
     */
    private CacheInvalidationMessage adaptMessage(com.ysmjjsy.goya.component.cache.redis.publish.CacheInvalidationMessage redisMessage) {
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
