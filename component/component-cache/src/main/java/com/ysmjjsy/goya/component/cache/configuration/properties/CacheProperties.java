package com.ysmjjsy.goya.component.cache.configuration.properties;

import com.ysmjjsy.goya.component.cache.constants.ICacheConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/21 22:59
 */
@Schema(description = "平台配置")
@ConfigurationProperties(prefix = ICacheConstants.PROPERTY_CACHE)
public record CacheProperties() {
}
