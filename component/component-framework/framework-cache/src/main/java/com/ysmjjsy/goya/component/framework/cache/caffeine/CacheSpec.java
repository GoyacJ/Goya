package com.ysmjjsy.goya.component.framework.cache.caffeine;

import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>单个缓存区域配置</p>
 *
 * @author goya
 * @since 2026/1/25 21:02
 */
public record CacheSpec(
        @DefaultValue("PT10M") Duration ttl,
        @DefaultValue("10000") long maximumSize
) {
    /**
     * 默认构造。
     */
    public CacheSpec() {
        this(Duration.ofMinutes(10), 10_000L);
    }
}