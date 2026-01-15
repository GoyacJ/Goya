package com.ysmjjsy.goya.component.cache.multilevel.core;

import com.ysmjjsy.goya.component.cache.core.support.CacheBloomFilter;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCache;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationMessage;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationMessageListener;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationPublisher;
import com.ysmjjsy.goya.component.cache.multilevel.publish.CacheInvalidationSubscriber;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>多级缓存编排器</p>
 * <p>协调 L1 和 L2 的交互，处理缓存失效通知，管理布隆过滤器</p>
 *
 * @author goya
 * @since 2026/1/15 11:34
 */
@Slf4j
public class MultiLevelCacheOrchestrator {

    /**
     * 缓存失效消息发布器（可选）
     */
    private final CacheInvalidationPublisher cacheInvalidationPublisher;

    /**
     * 布隆过滤器（可选）
     */
    private final CacheBloomFilter cacheBloomFilter;

    /**
     * 缓存失效消息订阅器（可选）
     */
    private final CacheInvalidationSubscriber cacheInvalidationSubscriber;

    /**
     * 本地缓存映射（用于处理失效通知时删除 L1）
     * Key: cacheName
     * Value: LocalCache 实例
     */
    private final Map<String, LocalCache<Object, Object>> localCacheMap = new ConcurrentHashMap<>();

    /**
     * 是否已订阅
     */
    private boolean subscribed = false;

    /**
     * 构造函数
     *
     * @param cacheInvalidationPublisher 缓存失效消息发布器（可选）
     * @param cacheBloomFilter           布隆过滤器（可选）
     * @param cacheInvalidationSubscriber 缓存失效消息订阅器（可选）
     */
    public MultiLevelCacheOrchestrator(
            CacheInvalidationPublisher cacheInvalidationPublisher,
            CacheBloomFilter cacheBloomFilter,
            CacheInvalidationSubscriber cacheInvalidationSubscriber) {
        this.cacheInvalidationPublisher = cacheInvalidationPublisher;
        this.cacheBloomFilter = cacheBloomFilter;
        this.cacheInvalidationSubscriber = cacheInvalidationSubscriber;
    }

    /**
     * 初始化编排器
     */
    public void init() {
        // 如果启用失效通知，订阅失效消息
        if (cacheInvalidationSubscriber != null) {
            subscribeInvalidationMessages();
        }
    }

    /**
     * 注册本地缓存
     *
     * @param cacheName  缓存名称
     * @param localCache 本地缓存实例
     */
    public void registerLocalCache(String cacheName, LocalCache<Object, Object> localCache) {
        localCacheMap.put(cacheName, localCache);
    }

    /**
     * 检查布隆过滤器是否可能包含该 key
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @return true 如果可能包含，false 如果肯定不包含
     */
    public boolean mightContain(String cacheName, Object key) {
        if (cacheBloomFilter == null) {
            return true; // 如果没有布隆过滤器，默认返回 true
        }
        return cacheBloomFilter.mightContain(cacheName, key);
    }

    /**
     * 将 key 添加到布隆过滤器
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    public void put(String cacheName, Object key) {
        if (cacheBloomFilter == null) {
            return; // 如果没有布隆过滤器，直接返回
        }
        cacheBloomFilter.put(cacheName, key);
    }

    /**
     * 发布缓存失效消息
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     */
    public void publishInvalidation(String cacheName, Object key) {
        if (cacheInvalidationPublisher == null) {
            return; // 如果没有失效通知发布器，直接返回
        }
        cacheInvalidationPublisher.publish(cacheName, key);
    }

    /**
     * 订阅失效消息
     */
    private void subscribeInvalidationMessages() {
        if (cacheInvalidationSubscriber == null || subscribed) {
            return;
        }

        try {
            CacheInvalidationMessageListener listener = this::handleInvalidationMessage;
            cacheInvalidationSubscriber.subscribe(listener);
            subscribed = true;
            log.debug("Subscribed to cache invalidation messages");
        } catch (Exception e) {
            log.error("Failed to subscribe to cache invalidation messages", e);
        }
    }

    /**
     * 处理失效消息
     *
     * @param message 失效消息
     */
    private void handleInvalidationMessage(CacheInvalidationMessage message) {
        if (message == null || !message.isValid()) {
            return;
        }

        try {
            LocalCache<Object, Object> localCache = localCacheMap.get(message.cacheName());
            if (localCache != null) {
                // 删除本地 L1 缓存
                localCache.delete(message.key());
                if (log.isTraceEnabled()) {
                    log.trace("Invalidated local cache from message: cacheName={}, key={}",
                            message.cacheName(), message.key());
                }
            }
        } catch (Exception e) {
            log.error("Failed to handle invalidation message: cacheName={}, key={}",
                    message.cacheName(), message.key(), e);
        }
    }

    /**
     * 销毁编排器
     */
    public void destroy() {
        // 取消订阅
        if (cacheInvalidationSubscriber != null && subscribed) {
            try {
                cacheInvalidationSubscriber.unsubscribe();
                subscribed = false;
                log.debug("Unsubscribed from cache invalidation messages");
            } catch (Exception e) {
                log.error("Failed to unsubscribe from cache invalidation messages", e);
            }
        }
    }
}
