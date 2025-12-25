package com.ysmjjsy.goya.component.cache.factory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.cache.configuration.properties.CacheProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/25 22:02
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineFactory {

    private final CacheProperties cacheProperties;

    public Caffeine<Object, Object> create() {
        return create(null);
    }

    public Caffeine<Object, Object> create(String cacheName) {
        return create(cacheName, false);
    }

    public Caffeine<Object, Object> create(String cacheName, boolean eternal) {
        return create(cacheName, eternal, null);
    }

    /**
     * <p>创建 Caffeine 缓存构建器</p>
     * <p>过期时间优先级逻辑：</p>
     * <ol>
     *     <li>如果配置中有该 cacheName 的配置 → 使用配置的 defaultTtl</li>
     *     <li>如果配置中没有 → 使用调用方法传入的 duration</li>
     *     <li>如果 duration 为空 → 使用默认配置的 defaultTtl</li>
     * </ol>
     *
     * @param cacheName 缓存名称（原始名称，用于配置查找）
     * @param eternal   是否永不过期
     * @param duration  调用方法传入的过期时间（可选）
     * @return Caffeine 构建器
     */
    public Caffeine<Object, Object> create(String cacheName, boolean eternal, Duration duration) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        
        // 获取配置（优先查找特定缓存配置，否则使用默认配置）
        CacheProperties.CacheConfig cacheConfig = cacheProperties.getCacheConfig(cacheName);
        boolean hasSpecificConfig = Objects.nonNull(cacheConfig);
        
        if (!hasSpecificConfig) {
            // 配置中没有该 cacheName，使用默认配置
            cacheConfig = cacheProperties.getCacheConfigByDefault(cacheName);
        }
        
        // 设置过期时间
        if (!eternal) {
            Duration ttlToUse;
            if (hasSpecificConfig) {
                // 优先级1：配置中有该 cacheName → 使用配置的 defaultTtl
                ttlToUse = cacheConfig.defaultTtl();
            } else {
                // 配置中没有该 cacheName
                if (Objects.nonNull(duration)) {
                    // 优先级2：使用调用方法传入的 duration
                    ttlToUse = duration;
                } else {
                    // 优先级3：duration 为空 → 使用默认配置的 defaultTtl
                    ttlToUse = cacheConfig.defaultTtl();
                }
            }
            builder.expireAfterWrite(ttlToUse);
        }
        
        // 设置最大容量
        builder.maximumSize(cacheConfig.caffeineMaxSize());
        
        // 设置统计（使用每个缓存的配置）
        if (Boolean.TRUE.equals(cacheConfig.enableStats())) {
            builder.recordStats();
        }
        
        return builder;
    }

    public boolean getAllowNullValues() {
        return getAllowNullValues(null);
    }

    public boolean getAllowNullValues(String cacheName) {
        return cacheProperties.getCacheConfigByDefault(cacheName).allowNullValues();
    }


}
