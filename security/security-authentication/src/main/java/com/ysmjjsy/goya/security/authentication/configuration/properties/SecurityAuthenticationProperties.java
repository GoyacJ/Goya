package com.ysmjjsy.goya.security.authentication.configuration.properties;

import com.ysmjjsy.goya.component.cache.annotation.PropertiesCache;
import com.ysmjjsy.goya.security.core.constants.ISecurityConstants;
import com.ysmjjsy.goya.security.core.enums.CertificateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>认证中心配置</p>
 *
 * @author goya
 * @since 2025/10/10 14:26
 */
@Schema(description = "认证中心配置")
@PropertiesCache
@ConfigurationProperties(prefix = ISecurityConstants.PROPERTY_PLATFORM_SECURITY_AUTHENTICATION)
public record SecurityAuthenticationProperties(

        @Schema(description = "token黑名单配置")
        @DefaultValue
        TokenBlackListConfig tokenBlackListConfig,

        @Schema(description = "jwt配置")
        @DefaultValue
        Jwk jwk
) {

    @Schema(description = "token黑名单配置")
    public record TokenBlackListConfig(

            @Schema(description = "token黑名单时间")
            @DefaultValue("PT5M")
            Duration tokenBlackListExpire,

            @Schema(description = "默认原因")
            @DefaultValue("Token is blacklisted")
            String defaultReason
    ) {

    }

    @Schema(description = "jwt配置")
    public record Jwk(
            CertificateEnum certificate,
            @DefaultValue("classpath*:certificate/ysmjjsy.jks")
            String jksKeyStore,
            @DefaultValue("ysmjjsy")
            String jksKeyPassword,
            @DefaultValue("ysmjjsy")
            String jksStorePassword,
            @DefaultValue("ysmjjsy")
            String jksKeyAlias
    ) {

    }
}
