package com.ysmjjsy.goya.component.cache.redis.publish;

import java.io.Serial;
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
public record CacheInvalidationMessage(
        /*
          缓存名称
         */
        String cacheName,

        /*
          缓存键
         */
        String key
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 验证消息有效性
     *
     * @return true 如果消息有效，false 如果消息无效
     */
    public boolean isValid() {
        return cacheName != null && !cacheName.isBlank()
                && key != null && !key.isBlank();
    }
}
