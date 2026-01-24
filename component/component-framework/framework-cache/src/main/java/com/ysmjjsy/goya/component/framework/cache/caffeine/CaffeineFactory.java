package com.ysmjjsy.goya.component.framework.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ysmjjsy.goya.component.framework.cache.autoconfigure.properties.CaffeineCacheProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/14 21:24
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineFactory {

    private final CaffeineCacheProperties caffeineCacheProperties;

    /**
     * 创建 Caffeine
     *
     * @return Caffeine
     */
    public Caffeine<Object, Object> createCaffeine() {
        return createCaffeine(caffeineCacheProperties.defaultConfig());
    }

    /**
     * 创建 Caffeine
     *
     * @param cacheName 缓存名称
     * @return Caffeine
     */
    public Caffeine<Object, Object> createCaffeine(String cacheName) {
        var config = caffeineCacheProperties.getCacheConfig(cacheName);
        return createCaffeine(config);
    }

    /**
     * 创建 Caffeine
     *
     * @param config Caffeine 配置
     * @return Caffeine
     */
    public Caffeine<Object, Object> createCaffeine(CaffeineCacheProperties.CaffeineCacheConfig config) {
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
        return builder;
    }

    /**
     * 创建 Caffeine Cache
     *
     * @param config config
     * @return Caffeine Cache
     */
    public Cache<Object, Object> createCaffeineCache(CaffeineCacheProperties.CaffeineCacheConfig config) {
        return createCaffeine(config).build();
    }

    /**
     * 创建 Caffeine Cache
     *
     * @param cacheName 缓存名称
     * @return Caffeine Cache
     */
    public Cache<Object, Object> createCaffeineCache(String cacheName) {
        var config = caffeineCacheProperties.getCacheConfig(cacheName);
        return createCaffeineCache(config);
    }

    /**
     * 创建 Spring Cache
     *
     * @param name 缓存名称
     * @return Spring Cache
     */
    public org.springframework.cache.Cache createSpringCache(String name) {
        var config = caffeineCacheProperties.getCacheConfig(name);
        boolean allowNullValues = Boolean.TRUE.equals(config.isAllowNullValues());

        Cache<Object, Object> nativeCache = createCaffeineCache(name);
        return new GoyaCaffeineCache(
                name,
                nativeCache,
                allowNullValues
        );
    }
}
