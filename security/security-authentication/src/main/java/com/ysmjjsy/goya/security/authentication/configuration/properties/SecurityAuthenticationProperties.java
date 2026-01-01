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
        Jwk jwk,

        @Schema(description = "密码Grant Type配置")
        @DefaultValue
        PasswordGrantConfig passwordGrantConfig,

        @Schema(description = "短信Grant Type配置")
        @DefaultValue
        SmsGrantConfig smsGrantConfig,

        @Schema(description = "OAuth2客户端配置（第三方登录）")
        @DefaultValue
        OAuth2ClientConfig oauth2ClientConfig,

        @Schema(description = "SSO配置")
        @DefaultValue
        SsoConfig ssoConfig
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

    @Schema(description = "密码Grant Type配置")
    public record PasswordGrantConfig(
            @Schema(description = "是否启用验证码")
            @DefaultValue("true")
            Boolean enableCaptcha,

            @Schema(description = "验证码类别")
            @DefaultValue("SPEC")
            String captchaCategory,

            @Schema(description = "密码最小长度")
            @DefaultValue("6")
            Integer minPasswordLength,

            @Schema(description = "密码最大长度")
            @DefaultValue("20")
            Integer maxPasswordLength
    ) {

    }

    @Schema(description = "短信Grant Type配置")
    public record SmsGrantConfig(
            @Schema(description = "短信验证码有效期（秒）")
            @DefaultValue("300")
            Integer codeExpireSeconds,

            @Schema(description = "短信验证码长度")
            @DefaultValue("6")
            Integer codeLength,

            @Schema(description = "短信验证码缓存名称")
            @DefaultValue("sms:verification:code")
            String cacheName,

            @Schema(description = "是否允许自动创建用户")
            @DefaultValue("false")
            Boolean allowAutoCreateUser
    ) {

    }

    @Schema(description = "OAuth2客户端配置（第三方登录）")
    public record OAuth2ClientConfig(
            @Schema(description = "微信登录配置")
            @DefaultValue
            WeChatConfig wechat,

            @Schema(description = "Gitee登录配置")
            @DefaultValue
            GiteeConfig gitee,

            @Schema(description = "GitHub登录配置")
            @DefaultValue
            GitHubConfig github,

            @Schema(description = "是否启用第三方登录")
            @DefaultValue("true")
            Boolean enabled
    ) {

        @Schema(description = "微信登录配置")
        public record WeChatConfig(
                @Schema(description = "是否启用")
                @DefaultValue("false")
                Boolean enabled,

                @Schema(description = "客户端ID")
                String clientId,

                @Schema(description = "客户端密钥")
                String clientSecret,

                @Schema(description = "授权范围")
                @DefaultValue("snsapi_login")
                String scope,

                @Schema(description = "回调地址")
                String redirectUri
        ) {

        }

        @Schema(description = "Gitee登录配置")
        public record GiteeConfig(
                @Schema(description = "是否启用")
                @DefaultValue("false")
                Boolean enabled,

                @Schema(description = "客户端ID")
                String clientId,

                @Schema(description = "客户端密钥")
                String clientSecret,

                @Schema(description = "授权范围")
                @DefaultValue("user_info")
                String scope,

                @Schema(description = "回调地址")
                String redirectUri
        ) {

        }

        @Schema(description = "GitHub登录配置")
        public record GitHubConfig(
                @Schema(description = "是否启用")
                @DefaultValue("false")
                Boolean enabled,

                @Schema(description = "客户端ID")
                String clientId,

                @Schema(description = "客户端密钥")
                String clientSecret,

                @Schema(description = "授权范围")
                @DefaultValue("read:user")
                String scope,

                @Schema(description = "回调地址")
                String redirectUri
        ) {

        }
    }

    @Schema(description = "SSO配置")
    public record SsoConfig(
            @Schema(description = "是否强制要求PKCE（OAuth2.1要求）")
            @DefaultValue("true")
            Boolean requirePkce,

            @Schema(description = "是否允许公开客户端（client_secret为none）")
            @DefaultValue("true")
            Boolean allowPublicClients,

            @Schema(description = "授权码有效期（秒）")
            @DefaultValue("300")
            Integer authorizationCodeExpireSeconds
    ) {

    }
}
