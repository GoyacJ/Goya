package com.ysmjjsy.goya.component.cache.multilevel.publish;

/**
 * <p>缓存失效消息发布器接口</p>
 * <p>用于在分布式环境下发布缓存失效消息，通知其他节点删除本地缓存（L1）</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * // 发布失效消息
 * cacheInvalidationPublisher.publish("userCache", "user:123");
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15
 */
public interface CacheInvalidationPublisher {

    /**
     * 发布缓存失效消息
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param <K>       键类型
     * @throws IllegalArgumentException 如果 cacheName 或 key 为 null
     */
    <K> void publish(String cacheName, K key);

    /**
     * 发布缓存失效消息（批量）
     *
     * @param cacheName 缓存名称
     * @param keys      缓存键列表
     * @param <K>       键类型
     * @throws IllegalArgumentException 如果 cacheName 或 keys 为 null
     */
    <K> void publish(String cacheName, Iterable<K> keys);
}
