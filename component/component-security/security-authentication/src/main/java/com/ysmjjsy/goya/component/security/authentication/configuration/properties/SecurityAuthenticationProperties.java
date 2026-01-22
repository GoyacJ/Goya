package com.ysmjjsy.goya.component.security.authentication.configuration.properties;

import com.ysmjjsy.goya.component.security.core.constants.SecurityConst;
import com.ysmjjsy.goya.component.security.core.enums.CertificateEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.List;

/**
 * <p>认证中心配置</p>
 *
 * @author goya
 * @since 2025/10/10 14:26
 */
@Schema(description = "认证中心配置")
@ConfigurationProperties(prefix = SecurityConst.PROPERTY_PLATFORM_SECURITY_AUTHENTICATION)
public record SecurityAuthenticationProperties(

        @Schema(description = "登录失败配置")
        LoginFailureConfig loginFailure,

        @Schema(description = "token黑名单配置")
        @DefaultValue
        TokenBlackListConfig tokenBlackList,

        @Schema(description = "jwt配置")
        @DefaultValue
        Jwk jwk,

        @Schema(description = "验证码配置")
        CaptchaConfig captcha,

        @Schema(description = "Token配置")
        @DefaultValue
        TokenConfig token,

        @Schema(description = "CORS配置")
        @DefaultValue
        CorsConfig cors,

        @Schema(description = "密码安全策略")
        @DefaultValue
        PasswordPolicy passwordPolicy
) {

    @Schema(description = "登录了失败配置")
    public record LoginFailureConfig(

            @Schema(description = "是否开启登录失败")
            @DefaultValue("true")
            boolean enabled,

            @Schema(description = "允许允许最大失败次数")
            @DefaultValue("5")
            Integer maxTimes,

            @Schema(description = "记录失败次数的缓存过期时间")
            @DefaultValue("PT2H")
            Duration expire
    ) {

    }

    @Schema(description = "验证码配置")
    public record CaptchaConfig(

            @Schema(description = "密码")
            @DefaultValue("true")
            boolean password,

            @Schema(description = "短信")
            @DefaultValue("false")
            boolean sms,

            @Schema(description = "社交")
            @DefaultValue("false")
            boolean social
    ) {

    }

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

    @Schema(description = "Token配置")
    public record TokenConfig(
            @Schema(description = "默认Token Type（BEARER或DPoP）")
            @DefaultValue("BEARER")
            String defaultTokenType,

            @Schema(description = "是否强制要求DPoP Proof（当Token Type为DPoP时）")
            @DefaultValue("true")
            Boolean requireDpopProof
    ) {
    }

    @Schema(description = "密码安全策略")
    public record PasswordPolicy(

            @Schema(description = "是否启用密码策略")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "最小长度")
            @DefaultValue("8")
            Integer minLength,

            @Schema(description = "最大长度")
            @DefaultValue("128")
            Integer maxLength,

            @Schema(description = "是否要求大写字母")
            @DefaultValue("true")
            Boolean requireUppercase,

            @Schema(description = "是否要求小写字母")
            @DefaultValue("true")
            Boolean requireLowercase,

            @Schema(description = "是否要求数字")
            @DefaultValue("true")
            Boolean requireDigit,

            @Schema(description = "是否要求特殊字符")
            @DefaultValue("true")
            Boolean requireSpecialChar,

            @Schema(description = "特殊字符集合")
            @DefaultValue("!@#$%^&*()_+-=[]{}|;:,.<>?")
            String specialChars,

            @Schema(description = "是否防止重复使用历史密码")
            @DefaultValue("true")
            Boolean preventReuse,

            @Schema(description = "历史密码检查数量（最近N个密码不能重复使用）")
            @DefaultValue("5")
            Integer historyCount,

            @Schema(description = "密码过期天数（0表示永不过期）")
            @DefaultValue("90")
            Integer expirationDays,

            @Schema(description = "密码过期前提醒天数")
            @DefaultValue("7")
            Integer expirationWarningDays
    ) {

    }

    @Schema(description = "CORS配置")
    public record CorsConfig(
            @Schema(description = "允许的来源（支持多个，生产环境必须配置具体域名，不允许使用*）")
            @DefaultValue({"http://localhost:8101"})
            List<String> allowedOrigins,

            @Schema(description = "允许的HTTP方法")
            @DefaultValue({"GET", "POST", "PUT", "DELETE", "OPTIONS"})
            List<String> allowedMethods,

            @Schema(description = "允许的请求头")
            @DefaultValue({"*"})
            List<String> allowedHeaders,

            @Schema(description = "是否允许携带凭证（Cookie等）")
            @DefaultValue("true")
            Boolean allowCredentials,

            @Schema(description = "预检请求的缓存时间（秒）")
            @DefaultValue("3600")
            Long maxAge
    ) {

    }
}
