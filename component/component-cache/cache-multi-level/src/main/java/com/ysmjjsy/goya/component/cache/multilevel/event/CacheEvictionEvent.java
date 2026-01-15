package com.ysmjjsy.goya.component.cache.multilevel.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

/**
 * 缓存失效事件
 *
 * <p>当缓存中的某个 key 被失效时发布此事件。
 * redis 模块会订阅此事件并转发到 Redis Pub/Sub，通知其他节点失效 L1。
 *
 * <p><b>事件流程：</b>
 * <ol>
 *   <li>{@link com.ysmjjsy.goya.component.cache.multilevel.core.GoyaCache#evict(Object)} 调用 {@link CacheEventPublisher#publishEviction(String, Object)}</li>
 *   <li>CacheEventPublisher 创建并发布 CacheEvictionEvent</li>
 *   <li>Spring 事件机制分发到所有监听器</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/26 14:48
 */
@Getter
public class CacheEvictionEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = -4095372960926359577L;
    /**
     * -- GETTER --
     *  获取缓存名称
     *
     */
    private final String cacheName;
    /**
     * -- GETTER --
     *  获取失效的缓存键
     *
     */
    private final Object key;

    /**
     * 构造函数
     *
     * @param cacheName 缓存名称
     * @param key 失效的缓存键
     */
    public CacheEvictionEvent(String cacheName, Object key) {
        super(cacheName);
        this.cacheName = cacheName;
        this.key = key;
    }

    @Override
    public String toString() {
        return "CacheEvictionEvent{cacheName='" + cacheName + "', key=" + key + '}';
    }
}
