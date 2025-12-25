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

    public Caffeine<Object, Object> create(String cacheName, boolean eternal, Duration duration) {
        CacheProperties.CacheConfig cacheConfig = cacheProperties.getCacheConfig(cacheName);
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        if (Objects.isNull(cacheConfig)) {
            cacheConfig = cacheProperties.getCacheConfigByDefault(cacheName);
            if (!eternal) {
                builder.expireAfterWrite(duration);
            }
        } else {
            builder.expireAfterWrite(cacheConfig.defaultTtl());
        }
        builder.maximumSize(cacheConfig.caffeineMaxSize());
        if (Boolean.TRUE.equals(cacheProperties.defaultConfig().enableStats())) {
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
