package com.ysmjjsy.goya.component.framework.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.framework.cache.properties.CaffeineCacheProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 22:34
 */
@Slf4j
@RequiredArgsConstructor
public class GoyaCaffeineCacheManager extends CaffeineCacheManager {

    private final CaffeineCacheProperties caffeineCacheProperties;

    @Override
    @NullMarked
    protected Cache<Object, Object> createNativeCaffeineCache(String name) {

        var config = caffeineCacheProperties.getCacheConfig(name);

        Caffeine<Object, Object> builder = Caffeine.newBuilder();

        // 最大容量
        if (config.maximumSize() != null && config.maximumSize() > 0) {
            builder.maximumSize(config.maximumSize());
        }

        // TTL（硬过期）
        if (config.defaultTtl() != null && !config.defaultTtl().isZero()) {
            builder.expireAfterWrite(config.defaultTtl());
        }

        // refreshAfterWrite（软刷新）
        if (config.refreshAfterWrite() != null
                && !config.refreshAfterWrite().isZero()) {

            builder.refreshAfterWrite(config.refreshAfterWrite());
        }

        return builder.build();
    }

    @Override
    @NullMarked
    protected org.springframework.cache.Cache createCaffeineCache(String name) {
        var config = caffeineCacheProperties.getCacheConfig(name);
        boolean allowNullValues = Boolean.TRUE.equals(config.isAllowNullValues());

        Cache<Object, Object> nativeCache = createNativeCaffeineCache(name);
        return new GoyaCaffeineCache(
                name,
                nativeCache,
                allowNullValues
        );
    }
}
