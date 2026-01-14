package com.ysmjjsy.goya.component.framework.cache.properties;

import com.ysmjjsy.goya.component.framework.constants.PropertyConst;
import io.swagger.v3.oas.annotations.media.Schema;
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
@ConfigurationProperties(PropertyConst.PROPERTY_CAFFEINE)
public record CaffeineCacheProperties(

        @Schema(description = "默认配置")
        CaffeineCacheConfig defaultConfig,

        @Schema(description = "缓存配置")
        Map<String, CaffeineCacheConfig> cacheConfigs
) {
    @Schema(description = "本地缓存配置")
    public record CaffeineCacheConfig(

            @Schema(description = "是否允许 null 值")
            Boolean isAllowNullValues,

            @Schema(description = "默认过期时间")
            @DefaultValue("PT5M")
            Duration defaultTtl,

            @Schema(description = "最大容量")
            @DefaultValue("100000")
            Long maximumSize,

            @Schema(description = "刷新时间（小于 TTL 才生效）")
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
