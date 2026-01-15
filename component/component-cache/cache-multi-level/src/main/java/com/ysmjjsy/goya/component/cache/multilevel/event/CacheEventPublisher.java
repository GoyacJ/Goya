package com.ysmjjsy.goya.component.cache.multilevel.event;

import org.springframework.context.ApplicationEventPublisher;

/**
 * 缓存事件发布器
 *
 * <p>使用 Spring ApplicationEvent 机制解耦核心模块与具体实现。
 * cache 模块发布事件，redis 模块订阅并转发到 Redis Pub/Sub。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>发布缓存失效事件（CacheEvictionEvent）</li>
 *   <li>发布缓存清空事件（CacheClearEvent）</li>
 * </ul>
 *
 * <p><b>与 Spring Cache 的集成点：</b>
 * <ul>
 *   <li>使用 Spring 的 {@link ApplicationEventPublisher} 发布事件</li>
 *   <li>事件会被 Spring 的事件机制分发到所有监听器</li>
 *   <li>redis 模块通过 {@link org.springframework.context.event.EventListener} 订阅事件</li>
 * </ul>
 *
 * <p><b>调用关系：</b>
 * <ul>
 *   <li>由 {@link com.ysmjjsy.goya.component.cache.multilevel.core.GoyaCache} 在 evict() 和 clear() 时调用</li>
 * </ul>
 *
 * <p><b>线程模型：</b>
 * <ul>
 *   <li>事件发布是同步的，但监听器的处理可能是异步的</li>
 *   <li>Spring 的事件机制保证线程安全</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:47
 */
public class CacheEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造函数
     *
     * @param eventPublisher Spring 事件发布器
     * @throws IllegalArgumentException 如果 eventPublisher 为 null
     */
    public CacheEventPublisher(ApplicationEventPublisher eventPublisher) {
        if (eventPublisher == null) {
            throw new IllegalArgumentException("ApplicationEventPublisher cannot be null");
        }
        this.eventPublisher = eventPublisher;
    }

    /**
     * 发布缓存失效事件
     *
     * <p>当缓存中的某个 key 被失效时，发布此事件。
     * redis 模块会订阅此事件并转发到 Redis Pub/Sub，通知其他节点失效 L1。
     *
     * <p><b>执行流程：</b>
     * <ol>
     *   <li>创建 CacheEvictionEvent 事件对象</li>
     *   <li>通过 ApplicationEventPublisher 发布事件</li>
     *   <li>Spring 事件机制分发到所有监听器</li>
     * </ol>
     *
     * @param cacheName 缓存名称
     * @param key 失效的缓存键
     */
    public void publishEviction(String cacheName, Object key) {
        if (cacheName == null || key == null) {
            return;
        }
        CacheEvictionEvent event = new CacheEvictionEvent(cacheName, key);
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布缓存清空事件
     *
     * <p>当整个缓存被清空时，发布此事件。
     * redis 模块会订阅此事件并转发到 Redis Pub/Sub，通知其他节点清空 L1。
     *
     * @param cacheName 缓存名称
     */
    public void publishClear(String cacheName) {
        if (cacheName == null) {
            return;
        }
        CacheClearEvent event = new CacheClearEvent(cacheName);
        eventPublisher.publishEvent(event);
    }
}
