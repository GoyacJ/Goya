package com.ysmjjsy.goya.component.cache.event;

import com.ysmjjsy.goya.component.cache.core.GoyaCache;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.util.Assert;

/**
 * 本地缓存失效事件
 *
 * <p>当收到其他节点的缓存失效消息时，发布此事件通知本地 L1 缓存失效。
 * 由 {@link GoyaCache} 订阅并处理。
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>节点 A 调用 {@code evict(key)}，失效本地 L1 和 L2</li>
 *   <li>节点 A 通过 Redis Pub/Sub 发布失效消息</li>
 *   <li>节点 B 收到失效消息，发布 {@code LocalCacheEvictionEvent}</li>
 *   <li>节点 B 的 {@code GoyaCache} 订阅事件，失效本地 L1</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26 14:49
 */

@Getter
public class LocalCacheEvictionEvent extends ApplicationEvent {

    /**
     * -- GETTER --
     *  获取发生失效操作的缓存名称。
     *
     */
    private final String cacheName;
    /**
     * -- GETTER --
     *  获取被失效的缓存键。
     *
     */
    private final Object key;

    /**
     * 构造一个新的 {@code LocalCacheEvictionEvent}。
     *
     * @param source 事件源（通常是发布事件的对象）
     * @param cacheName 发生失效操作的缓存名称
     * @param key 被失效的缓存键
     * @throws IllegalArgumentException 如果 cacheName 为空或 key 为 null
     */
    public LocalCacheEvictionEvent(Object source, String cacheName, Object key) {
        super(source);
        Assert.hasText(cacheName, "Cache name must not be empty");
        Assert.notNull(key, "Key must not be null");
        this.cacheName = cacheName;
        this.key = key;
    }

    @Override
    public String toString() {
        return "LocalCacheEvictionEvent{" +
                "cacheName='" + cacheName + '\'' +
                ", key=" + key +
                '}';
    }
}

