package com.ysmjjsy.goya.security.resource.server.configuration.properties;

import com.ysmjjsy.goya.component.common.properties.IProperties;
import com.ysmjjsy.goya.security.core.constants.ISecurityConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/11 21:16
 */
@ConfigurationProperties(prefix = ISecurityConstants.PROPERTY_PLATFORM_SECURITY_RESOURCE)
@Schema(description = "资源中心配置")
public record SecurityResourceProperties(

)  {
}
