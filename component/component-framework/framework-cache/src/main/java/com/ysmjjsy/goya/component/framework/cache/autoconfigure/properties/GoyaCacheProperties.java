package com.ysmjjsy.goya.component.framework.cache.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.cache.caffeine.CacheSpec;
import com.ysmjjsy.goya.component.framework.cache.constants.CacheConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

/**
 * <p>配置项</p>
 *
 * @param defaultTtl 默认 TTL（对未配置 cacheSpec 的 cacheName 生效）
 * @param defaultMaximumSize 默认最大容量
 * @param recordStats 是否记录统计（命中率等，便于观测）
 * @param allowNullValues 是否允许缓存 null（允许时会存入一个 Null 标记）
 * @param caches 按缓存名配置（每个 cacheName 一个 CacheSpec）
 *
 * @author goya
 * @since 2026/1/12 23:05
 */
@ConfigurationProperties(CacheConst.PROPERTY_CACHE)
public record GoyaCacheProperties(
        @DefaultValue("PT10M") Duration defaultTtl,
        @DefaultValue("10000") long defaultMaximumSize,
        @DefaultValue("false") boolean recordStats,
        @DefaultValue("false") boolean allowNullValues,
        Map<String, CacheSpec> caches
) {
}
