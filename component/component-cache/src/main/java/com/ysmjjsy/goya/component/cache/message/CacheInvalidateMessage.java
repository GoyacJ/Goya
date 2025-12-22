package com.ysmjjsy.goya.component.cache.message;

import java.io.Serializable;

/**
 * <p>缓存失效消息</p>
 * <p>用于多节点间同步缓存失效事件，通过 Redis Pub/Sub 实现</p>
 * <p>使用场景：</p>
 * <ul>
 *     <li>单个键失效：删除或更新单个缓存项时</li>
 *     <li>整个缓存清空：清空某个缓存命名空间时</li>
 *     <li>全部清空：清空所有缓存时</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/22
 */
public record CacheInvalidateMessage(
        /**
         * 失效类型
         */
        InvalidateType type,

        /**
         * 缓存名称
         */
        String cacheName,

        /**
         * 缓存键（单个键失效时使用）
         */
        Object key,

        /**
         * 发送节点 ID（避免自己处理自己发的消息）
         */
        String nodeId,

        /**
         * 时间戳
         */
        long timestamp,

        /**
         * 版本号（用于一致性控制，仅 KEY 类型失效时使用）
         */
        Long version
) implements Serializable {

    /**
     * 失效类型枚举
     */
    public enum InvalidateType {
        /**
         * 单个键失效
         */
        KEY,

        /**
         * 整个缓存清空
         */
        CACHE,

        /**
         * 全部清空
         */
        ALL
    }

    /**
     * 创建单个键失效消息
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param nodeId    节点 ID
     * @return 失效消息
     */
    public static CacheInvalidateMessage ofKey(String cacheName, Object key, String nodeId) {
        return new CacheInvalidateMessage(
                InvalidateType.KEY,
                cacheName,
                key,
                nodeId,
                System.currentTimeMillis(),
                null
        );
    }

    /**
     * 创建单个键失效消息（带版本号）
     *
     * @param cacheName 缓存名称
     * @param key       缓存键
     * @param nodeId    节点 ID
     * @param version   版本号
     * @return 失效消息
     */
    public static CacheInvalidateMessage ofKey(String cacheName, Object key, String nodeId, long version) {
        return new CacheInvalidateMessage(
                InvalidateType.KEY,
                cacheName,
                key,
                nodeId,
                System.currentTimeMillis(),
                version
        );
    }

    /**
     * 创建整个缓存清空消息
     *
     * @param cacheName 缓存名称
     * @param nodeId    节点 ID
     * @return 失效消息
     */
    public static CacheInvalidateMessage ofCache(String cacheName, String nodeId) {
        return new CacheInvalidateMessage(
                InvalidateType.CACHE,
                cacheName,
                null,
                nodeId,
                System.currentTimeMillis(),
                null
        );
    }

    /**
     * 创建全部清空消息
     *
     * @param nodeId 节点 ID
     * @return 失效消息
     */
    public static CacheInvalidateMessage ofAll(String nodeId) {
        return new CacheInvalidateMessage(
                InvalidateType.ALL,
                null,
                null,
                nodeId,
                System.currentTimeMillis(),
                null
        );
    }
}

