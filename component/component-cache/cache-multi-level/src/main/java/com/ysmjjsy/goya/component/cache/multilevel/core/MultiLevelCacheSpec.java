package com.ysmjjsy.goya.component.cache.multilevel.core;

import java.time.Duration;

/**
 * <p>多级缓存配置规范</p>
 * <p>定义多级缓存的配置参数，包括 L1、L2 的 TTL、容量、是否启用布隆过滤器等</p>
 * <p>使用示例：</p>
 * <pre>{@code
 * MultiLevelCacheSpec spec = new MultiLevelCacheSpec(
 *     "userCache",
 *     Duration.ofMinutes(5),    // L1 TTL
 *     Duration.ofMinutes(30),   // L2 TTL
 *     10000L,                   // L1 最大容量
 *     true,                      // 启用布隆过滤器
 *     true,                      // 启用失效通知
 *     false                      // 不允许 null 值
 * );
 * }</pre>
 *
 * @author goya
 * @since 2026/1/15 11:41
 */
public record MultiLevelCacheSpec(
        /*
          缓存名称
         */
        String cacheName,

        /*
          L1 本地缓存 TTL
         */
        Duration localTtl,

        /*
          L2 远程缓存 TTL
         */
        Duration remoteTtl,

        /*
          L1 本地缓存最大容量
         */
        Long localMaximumSize,

        /*
          是否启用布隆过滤器（用于缓存穿透防护）
         */
        Boolean enableBloomFilter,

        /*
          是否启用失效通知（用于分布式环境下 L1 缓存一致性）
         */
        Boolean enableInvalidationNotify,

        /*
          是否允许 null 值
         */
        Boolean allowNullValues
) {
    /**
     * 验证配置规范的有效性
     *
     * @return true 如果配置有效，false 如果配置无效
     */
    public boolean isValid() {
        return cacheName != null && !cacheName.isBlank()
                && localTtl != null && !localTtl.isNegative()
                && remoteTtl != null && !remoteTtl.isNegative()
                && localMaximumSize != null && localMaximumSize > 0
                && enableBloomFilter != null
                && enableInvalidationNotify != null
                && allowNullValues != null;
    }
}
