package com.ysmjjsy.goya.component.framework.cache.autoconfigure.properties;

import com.ysmjjsy.goya.component.framework.cache.constants.CacheConst;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 23:05
 */
@ConfigurationProperties(CacheConst.PROPERTY_CAFFEINE)
public record CaffeineCacheProperties(

        /*
          默认配置
         */
        @DefaultValue
        CaffeineCacheConfig defaultConfig,

        /*
          缓存配置
         */
        @DefaultValue
        Map<String, CaffeineCacheConfig> cacheConfigs
) {

    /**
     * Caffeine 缓存配置
     * @param isAllowNullValues
     * @param defaultTtl
     * @param maximumSize
     * @param refreshAfterWrite
     */
    public record CaffeineCacheConfig(

            /*
              是否允许 null 值
             */
            Boolean isAllowNullValues,

            /*
              默认过期时间
             */
            @DefaultValue("PT5M")
            Duration defaultTtl,

            /*
              最大容量
             */
            @DefaultValue("100000")
            Long maximumSize,

            /*
              刷新时间（小于 TTL 才生效）
             */
            Duration refreshAfterWrite
    ) {

    }

    /**
     * 获取缓存配置
     *
     * @param cacheName 缓存名称
     * @return 缓存配置
     */
    public CaffeineCacheConfig getCacheConfig(String cacheName) {
        return cacheConfigs.getOrDefault(cacheName, defaultConfig);
    }
}
