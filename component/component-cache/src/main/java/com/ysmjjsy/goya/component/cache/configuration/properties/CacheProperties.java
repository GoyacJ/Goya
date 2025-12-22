package com.ysmjjsy.goya.component.cache.configuration.properties;

import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import com.ysmjjsy.goya.component.cache.enums.CacheTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>缓存配置属性</p>
 * <p>支持配置缓存类型、键前缀、默认过期时间等属性</p>
 *
 * @author goya
 * @since 2025/12/21 22:59
 * @see ICacheConstants
 */
@Schema(description = "缓存配置属性")
@ConfigurationProperties(prefix = ICacheConstants.PROPERTY_CACHE)
public record CacheProperties(
        /*
          缓存类型（caffeine / redis）
          默认使用 caffeine 本地缓存
         */
        @Schema(description = "缓存类型", example = "caffeine")
        CacheTypeEnum type,
        
        /*
          缓存键前缀
          用于区分不同应用的缓存数据
         */
        @Schema(description = "缓存键前缀", example = "goya:")
        @DefaultValue("goya:")
        String keyPrefix,
        
        /*
          默认过期时间
          当未指定过期时间时使用此值
         */
        @Schema(description = "默认过期时间", example = "PT30M")
        @DefaultValue("PT30M")
        Duration defaultTtl,
        
        /*
          是否启用缓存统计
         */
        @Schema(description = "是否启用缓存统计", example = "false")
        @DefaultValue("false")
        Boolean enableStats
) {
}
