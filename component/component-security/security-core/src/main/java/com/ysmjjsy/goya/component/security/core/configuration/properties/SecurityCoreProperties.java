package com.ysmjjsy.goya.component.security.core.configuration.properties;

import com.ysmjjsy.goya.component.security.core.constants.SecurityConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 16:51
 */
@Schema(defaultValue = "Security 核心配置")
@ConfigurationProperties(prefix = SecurityConst.PROPERTY_PLATFORM_SECURITY)
public record SecurityCoreProperties(

        @Schema(defaultValue = "认证中心地址")
        String authServiceUri,

        @Schema(defaultValue = "认证中心名称")
        String authServiceName
) {
}
