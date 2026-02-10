package com.ysmjjsy.goya.component.security.oauth2.configuration.properties;

import com.ysmjjsy.goya.component.security.core.constants.SecurityConst;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>OAuth2 配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(defaultValue = "Security OAuth2 配置")
@ConfigurationProperties(prefix = SecurityConst.PROPERTY_PLATFORM_SECURITY_OAUTH2)
public record SecurityOAuth2Properties(

        @Schema(defaultValue = "是否启用OAuth2模块")
        @DefaultValue("true")
        boolean enabled,

        @Schema(defaultValue = "部署模式")
        @DefaultValue("EMBEDDED")
        DeploymentMode deploymentMode,

        @Schema(defaultValue = "Issuer")
        @DefaultValue("")
        String issuer,

        @Schema(defaultValue = "是否启用OIDC")
        @DefaultValue("true")
        boolean oidcEnabled,

        @Schema(defaultValue = "预认证授权类型")
        @DefaultValue("urn:goya:grant-type:pre-auth-code")
        String preAuthCodeGrantType,

        @Schema(defaultValue = "公开客户端是否强制PKCE")
        @DefaultValue("true")
        boolean requirePkceForPublicClients,

        @Schema(defaultValue = "Web默认AccessToken格式")
        @DefaultValue("JWT")
        String webAccessTokenFormat,

        @Schema(defaultValue = "移动端默认AccessToken格式")
        @DefaultValue("OPAQUE")
        String mobileAppAccessTokenFormat,

        @Schema(defaultValue = "小程序默认AccessToken格式")
        @DefaultValue("OPAQUE")
        String miniProgramAccessTokenFormat,

        @Schema(defaultValue = "JWT AccessToken有效期")
        @DefaultValue("PT15M")
        Duration accessTokenTtlJwt,

        @Schema(defaultValue = "Opaque AccessToken有效期")
        @DefaultValue("PT30M")
        Duration accessTokenTtlOpaque,

        @Schema(defaultValue = "RefreshToken有效期")
        @DefaultValue("P14D")
        Duration refreshTokenTtl,

        @Schema(defaultValue = "是否复用RefreshToken")
        @DefaultValue("false")
        boolean reuseRefreshTokens,

        @Schema(defaultValue = "吊销缓存命名空间")
        @DefaultValue("goya:security:token:revoked")
        String revocationCacheName,

        @Schema(defaultValue = "吊销广播主题")
        @DefaultValue("security-token-revoked-topic")
        String revocationTopic,

        @Schema(defaultValue = "密钥轮换配置")
        @DefaultValue
        Keys keys,

        @Schema(defaultValue = "预认证扩展配置")
        @DefaultValue
        PreAuth preAuth,

        @Schema(defaultValue = "端点配置")
        @DefaultValue
        Endpoints endpoints
) {

    public enum DeploymentMode {
        EMBEDDED,
        AUTH_CENTER
    }

    @Schema(defaultValue = "OAuth2端点")
    public record Endpoints(

            @DefaultValue("/oauth2/authorize")
            String authorizationEndpoint,

            @DefaultValue("/oauth2/token")
            String tokenEndpoint,

            @DefaultValue("/oauth2/jwks")
            String jwkSetEndpoint,

            @DefaultValue("/oauth2/revoke")
            String revocationEndpoint,

            @DefaultValue("/oauth2/introspect")
            String introspectionEndpoint,

            @DefaultValue("/connect/logout")
            String oidcLogoutEndpoint
    ) {
    }

    @Schema(defaultValue = "密钥轮换配置")
    public record Keys(

            @Schema(defaultValue = "轮换周期")
            @DefaultValue("P30D")
            Duration rotationInterval,

            @Schema(defaultValue = "旧密钥重叠验签窗口")
            @DefaultValue("P7D")
            Duration overlap,

            @Schema(defaultValue = "是否允许回退到内存JWK")
            @DefaultValue("false")
            boolean allowInMemoryFallback
    ) {
    }

    @Schema(defaultValue = "预认证扩展配置")
    public record PreAuth(

            @Schema(defaultValue = "是否强制校验 pre_auth_code 与 client_id 绑定")
            @DefaultValue("true")
            boolean requireClientBinding
    ) {
    }
}
