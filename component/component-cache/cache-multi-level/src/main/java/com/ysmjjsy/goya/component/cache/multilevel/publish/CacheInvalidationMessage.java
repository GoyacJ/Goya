package com.ysmjjsy.goya.component.cache.multilevel.publish;

import java.io.Serializable;

/**
 * <p>缓存失效消息</p>
 * <p>用于在分布式环境下通知其他节点删除本地缓存（L1）</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * CacheInvalidationMessage message = new CacheInvalidationMessage("userCache", "user:123");
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15
 */
public interface CacheInvalidationMessage extends Serializable {

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    String cacheName();

    /**
     * 获取缓存键（序列化后的字符串）
     *
     * @return 缓存键
     */
    String key();

    /**
     * 验证消息有效性
     *
     * @return true 如果消息有效，false 如果消息无效
     */
    default boolean isValid() {
        String cacheName = cacheName();
        String key = key();
        return cacheName != null && !cacheName.isBlank()
                && key != null && !key.isBlank();
    }
}
