package com.ysmjjsy.goya.security.resource.server.configuration.properties;

import com.ysmjjsy.goya.component.cache.annotation.PropertiesCache;
import com.ysmjjsy.goya.security.core.constants.ISecurityConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * <p>资源服务器配置</p>
 * <p>配置OAuth2资源服务器的核心功能：</p>
 * <ul>
 *   <li>JWT Token验证</li>
 *   <li>DPoP Proof验证</li>
 *   <li>Token黑名单检查</li>
 *   <li>多租户支持</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/11 21:16
 */
@Schema(description = "资源服务器配置")
@PropertiesCache
@ConfigurationProperties(prefix = ISecurityConstants.PROPERTY_PLATFORM_SECURITY_RESOURCE)
public record SecurityResourceProperties(

        @Schema(description = "JWT配置")
        @DefaultValue
        JwtConfig jwt,

        @Schema(description = "DPoP配置")
        @DefaultValue
        DPoPConfig dpop,

        @Schema(description = "Token黑名单配置")
        @DefaultValue
        TokenBlacklistConfig tokenBlacklist,

        @Schema(description = "多租户配置")
        @DefaultValue
        MultiTenantConfig multiTenant
) {

    @Schema(description = "JWT配置")
    public record JwtConfig(
            @Schema(description = "授权服务器Issuer URI（用于JWT验证）")
            String issuerUri,

            @Schema(description = "JWK Set URI（可选，如果未提供则从issuer-uri/.well-known/jwks.json获取）")
            String jwkSetUri,

            @Schema(description = "JWT Audience（可选，用于验证aud claim）")
            List<String> audiences,

            @Schema(description = "是否验证JWT类型（typ claim）")
            @DefaultValue("true")
            Boolean validateType
    ) {
    }

    @Schema(description = "DPoP配置")
    public record DPoPConfig(
            @Schema(description = "是否启用DPoP验证")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "是否强制要求DPoP（如果Token包含cnf.jkt claim）")
            @DefaultValue("true")
            Boolean requireDpopForBoundTokens
    ) {
    }

    @Schema(description = "Token黑名单配置")
    public record TokenBlacklistConfig(
            @Schema(description = "是否启用Token黑名单检查")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "Token黑名单缓存名称（需与认证服务器一致）")
            @DefaultValue("security:authentication:blacklist:")
            String cacheName
    ) {
    }

    @Schema(description = "多租户配置")
    public record MultiTenantConfig(
            @Schema(description = "是否启用多租户支持")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "默认租户ID（当JWT中不包含tenant_id时使用）")
            @DefaultValue("public")
            String defaultTenantId
    ) {
    }
}
