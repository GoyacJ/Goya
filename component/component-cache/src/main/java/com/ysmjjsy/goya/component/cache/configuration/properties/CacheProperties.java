package com.ysmjjsy.goya.component.cache.configuration.properties;

import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>缓存配置属性</p>
 * <p>支持配置键前缀、默认过期时间、本地缓存参数等</p>
 *
 * @author goya
 * @since 2025/12/22
 * @see ICacheConstants
 */
@Schema(description = "缓存配置属性")
@ConfigurationProperties(prefix = ICacheConstants.PROPERTY_CACHE)
public record CacheProperties(
        /**
         * 缓存键前缀
         * 用于区分不同应用的缓存数据
         */
        @Schema(description = "缓存键前缀", example = "goya:")
        @DefaultValue("goya:")
        String cachePrefix,

        /**
         * 默认过期时间
         * 当未指定过期时间时使用此值
         */
        @Schema(description = "默认过期时间", example = "PT30M")
        @DefaultValue("PT30M")
        Duration defaultTtl,

        /**
         * 是否启用缓存统计
         */
        @Schema(description = "是否启用缓存统计", example = "false")
        @DefaultValue("false")
        Boolean enableStats,

        /**
         * 本地缓存（Caffeine）最大容量
         */
        @Schema(description = "本地缓存最大容量", example = "10000")
        @DefaultValue("10000")
        Integer caffeineMaxSize,

        /**
         * 本地缓存（Caffeine）过期时间
         * 如果不设置，使用 defaultTtl
         */
        @Schema(description = "本地缓存过期时间", example = "PT5M")
        Duration caffeineTtl,

        /**
         * 缓存失效消息主题
         */
        @Schema(description = "缓存失效消息主题", example = "cache:invalidate")
        @DefaultValue("cache:invalidate")
        String invalidateTopic
) {

    /**
     * 获取 Caffeine 最大容量，提供默认值
     *
     * @return Caffeine 最大容量
     */
    public Integer caffeineMaxSize() {
        return caffeineMaxSize != null ? caffeineMaxSize : 10000;
    }

    /**
     * 获取 Caffeine TTL，提供默认值
     *
     * @return Caffeine TTL
     */
    public Duration caffeineTtl() {
        return caffeineTtl != null ? caffeineTtl : defaultTtl;
    }

    /**
     * 获取失效消息主题，提供默认值
     *
     * @return 失效消息主题
     */
    public String invalidateTopic() {
        return invalidateTopic != null ? invalidateTopic : "cache:invalidate";
    }
}
