package com.ysmjjsy.goya.component.cache.redis.publish;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.redis.constants.RedisConst;
import com.ysmjjsy.goya.component.cache.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicBoolean;

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
public class RedisInvalidationSubscriber {

    /**
     * Redis 服务
     */
    private final RedisService redisService;

    /**
     * 订阅 ID
     */
    private volatile Integer subscriptionId;

    /**
     * 消息监听器
     */
    private volatile RedisInvalidationMessageListener listener;

    /**
     * 是否已订阅（使用 AtomicBoolean 保证线程安全）
     */
    private final AtomicBoolean subscribed = new AtomicBoolean(false);

    public void subscribe(RedisInvalidationMessageListener listener) {
        if (!subscribed.compareAndSet(false, true)) {
            throw new IllegalStateException("Already subscribed to cache invalidation messages");
        }
        if (listener == null) {
            subscribed.set(false); // 恢复状态
            throw new IllegalArgumentException("Message listener cannot be null");
        }

        this.listener = listener;

        try {
            // 使用 RedisService 订阅失效消息
            // 注意：使用完全限定名避免与 cache-multi-level 的 CacheInvalidationMessage 冲突
            Integer id = redisService.subscribe(
                    RedisConst.CACHE_INVALIDATION_CHANNEL,
                    RedisInvalidationMessage.class,
                    (_, message) -> {
                        if (message != null && message.isValid()) {
                            // 将 cache-redis 的消息适配为 cache-multi-level 的消息格式
                            RedisInvalidationMessage adaptedMessage = adaptMessage(message);
                            listener.onMessage(adaptedMessage);
                        }
                    }
            );

            subscriptionId = id;
            log.debug("Subscribed to cache invalidation channel: channel={}, subscriptionId={}",
                    RedisConst.CACHE_INVALIDATION_CHANNEL, id);
        } catch (Exception e) {
            subscribed.set(false); // 订阅失败，恢复状态
            subscriptionId = null;
            this.listener = null;
            log.error("Failed to subscribe to cache invalidation channel: channel={}",
                    RedisConst.CACHE_INVALIDATION_CHANNEL, e);
            throw new CacheException("Failed to subscribe to cache invalidation channel", e);
        }
    }

    public void unsubscribe() {
        if (!subscribed.get()) {
            return;
        }

        Integer id = subscriptionId; // 保存引用，避免在设置为 null 后无法使用
        if (id == null) {
            subscribed.set(false);
            return;
        }

        try {
            redisService.unsubscribe(RedisConst.CACHE_INVALIDATION_CHANNEL, id);
            subscribed.set(false);
            subscriptionId = null;
            listener = null;
            log.debug("Unsubscribed from cache invalidation channel: subscriptionId={}", id);
        } catch (Exception e) {
            log.error("Failed to unsubscribe from cache invalidation channel: subscriptionId={}", id, e);
            throw new CacheException("Failed to unsubscribe from cache invalidation channel", e);
        }
    }

    /**
     * 将 cache-redis 的消息适配为 cache-multi-level 的消息格式
     *
     * @param redisMessage cache-redis 的消息
     * @return cache-multi-level 的消息格式
     */
    private RedisInvalidationMessage adaptMessage(RedisInvalidationMessage redisMessage) {
        return new RedisInvalidationMessage(redisMessage.cacheName(), redisMessage.key());
    }
}
