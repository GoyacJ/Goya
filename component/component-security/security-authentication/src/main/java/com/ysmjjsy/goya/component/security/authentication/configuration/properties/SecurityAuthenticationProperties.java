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

        @Schema(description = "登录方式配置")
        @DefaultValue
        LoginConfig login,

        @Schema(description = "Token配置")
        @DefaultValue
        TokenConfig token,

        @Schema(description = "短信配置")
        @DefaultValue
        SmsConfig sms,

        @Schema(description = "CORS配置")
        @DefaultValue
        CorsConfig cors,

        @Schema(description = "密码安全策略")
        @DefaultValue
        PasswordPolicy passwordPolicy,

        @Schema(description = "MFA 多因素认证配置")
        @DefaultValue
        MfaConfig mfa,

        @Schema(description = "密码重置配置")
        @DefaultValue
        PasswordResetConfig passwordReset,

        @Schema(description = "会话管理配置")
        @DefaultValue
        SessionConfig session,

        @Schema(description = "用户注册配置")
        @DefaultValue
        RegistrationConfig registration,

        @Schema(description = "API 限流配置")
        @DefaultValue
        RateLimitConfig rateLimit
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
            Duration expire,

            @Schema(description = "账号锁定持续时间（达到最大失败次数后锁定多长时间）")
            @DefaultValue("PT30M")
            Duration lockoutDuration,

            @Schema(description = "是否启用自动解锁（基于锁定持续时间）")
            @DefaultValue("true")
            Boolean autoUnlockEnabled
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

    @Schema(description = "登录方式配置")
    public record LoginConfig(
            @Schema(description = "允许密码登录")
            @DefaultValue("true")
            boolean allowPasswordLogin,

            @Schema(description = "允许短信OTP登录")
            @DefaultValue("true")
            boolean allowSmsLogin,

            @Schema(description = "允许社交登录")
            @DefaultValue("true")
            boolean allowSocialLogin
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

    @Schema(description = "短信配置")
    public record SmsConfig(
            @Schema(description = "验证码长度")
            @DefaultValue("6")
            Integer codeLength,

            @Schema(description = "验证码有效期")
            @DefaultValue("PT5M")
            Duration codeTtl,

            @Schema(description = "发送频率限制（次数/时间）")
            @DefaultValue("1")
            Integer rateLimit,

            @Schema(description = "每日发送限制")
            @DefaultValue("5")
            Integer dailyLimit
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

    @Schema(description = "MFA 多因素认证配置")
    public record MfaConfig(
            @Schema(description = "是否启用 MFA 功能")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "是否强制要求 MFA")
            @DefaultValue("false")
            Boolean required,

            @Schema(description = "默认验证类型（TOTP/SMS）")
            @DefaultValue("TOTP")
            String defaultType,

            @Schema(description = "TOTP 配置")
            @DefaultValue
            TotpConfig totp,

            @Schema(description = "备用验证码配置")
            @DefaultValue
            BackupCodesConfig backupCodes,

            @Schema(description = "MFA 挑战配置")
            @DefaultValue
            ChallengeConfig challenge
    ) {
        @Schema(description = "TOTP 配置")
        public record TotpConfig(
                @Schema(description = "Issuer 名称（显示在 Authenticator 应用中）")
                @DefaultValue("Goya Auth")
                String issuer,

                @Schema(description = "时间步长（秒）")
                @DefaultValue("30")
                Integer timeStep,

                @Schema(description = "验证码位数")
                @DefaultValue("6")
                Integer digits,

                @Schema(description = "时间窗口容差（±N 个时间窗口）")
                @DefaultValue("1")
                Integer window
        ) {
        }

        @Schema(description = "备用验证码配置")
        public record BackupCodesConfig(
                @Schema(description = "备用验证码数量")
                @DefaultValue("10")
                Integer count,

                @Schema(description = "备用验证码长度")
                @DefaultValue("8")
                Integer length
        ) {
        }

        @Schema(description = "MFA 挑战配置")
        public record ChallengeConfig(
                @Schema(description = "挑战过期时间")
                @DefaultValue("PT5M")
                Duration ttl,

                @Schema(description = "最大重试次数")
                @DefaultValue("5")
                Integer maxRetries,

                @Schema(description = "重试锁定时间")
                @DefaultValue("PT15M")
                Duration lockoutDuration
        ) {
        }
    }

    @Schema(description = "密码重置配置")
    public record PasswordResetConfig(
            @Schema(description = "是否启用密码重置功能")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "重置令牌有效期")
            @DefaultValue("PT1H")
            Duration tokenTtl,

            @Schema(description = "重置令牌长度")
            @DefaultValue("32")
            Integer tokenLength,

            @Schema(description = "发送重置请求的频率限制（次数/时间）")
            @DefaultValue("3")
            Integer requestRateLimit,

            @Schema(description = "发送重置请求的频率限制时间窗口")
            @DefaultValue("PT1H")
            Duration requestRateLimitWindow,

            @Schema(description = "是否支持邮箱重置")
            @DefaultValue("true")
            Boolean allowEmailReset,

            @Schema(description = "是否支持短信重置")
            @DefaultValue("true")
            Boolean allowSmsReset
    ) {
    }

    @Schema(description = "会话管理配置")
    public record SessionConfig(
            @Schema(description = "是否启用会话管理功能")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "会话超时时间（默认 30 分钟）")
            @DefaultValue("PT30M")
            Duration timeout,

            @Schema(description = "最大并发会话数（0 表示不限制）")
            @DefaultValue("0")
            Integer maxConcurrentSessions,

            @Schema(description = "是否允许用户查看自己的会话列表")
            @DefaultValue("true")
            Boolean allowUserViewSessions,

            @Schema(description = "是否允许用户强制下线自己的其他会话")
            @DefaultValue("true")
            Boolean allowUserRevokeSessions
    ) {
    }

    @Schema(description = "用户注册配置")
    public record RegistrationConfig(
            @Schema(description = "是否启用用户注册功能")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "是否允许邮箱注册")
            @DefaultValue("true")
            Boolean allowEmailRegistration,

            @Schema(description = "是否允许手机号注册")
            @DefaultValue("true")
            Boolean allowPhoneRegistration,

            @Schema(description = "是否要求邮箱验证")
            @DefaultValue("true")
            Boolean requireEmailVerification,

            @Schema(description = "是否要求手机号验证")
            @DefaultValue("true")
            Boolean requirePhoneVerification,

            @Schema(description = "注册后是否自动激活账号")
            @DefaultValue("false")
            Boolean autoActivate,

            @Schema(description = "注册请求频率限制（次数/时间）")
            @DefaultValue("5")
            Integer requestRateLimit,

            @Schema(description = "注册请求频率限制时间窗口")
            @DefaultValue("PT1H")
            Duration requestRateLimitWindow
    ) {
    }

    @Schema(description = "API 限流配置")
    public record RateLimitConfig(
            @Schema(description = "是否启用 API 限流")
            @DefaultValue("true")
            Boolean enabled,

            @Schema(description = "登录接口限流配置")
            @DefaultValue
            LoginRateLimitConfig login,

            @Schema(description = "Token 刷新接口限流配置")
            @DefaultValue
            TokenRefreshRateLimitConfig tokenRefresh,

            @Schema(description = "IP 级别限流配置")
            @DefaultValue
            IpRateLimitConfig ip
    ) {
        @Schema(description = "登录接口限流配置")
        public record LoginRateLimitConfig(
                @Schema(description = "是否启用登录接口限流")
                @DefaultValue("true")
                Boolean enabled,

                @Schema(description = "每个 IP 每分钟允许的登录次数")
                @DefaultValue("5")
                Integer ipLimitPerMinute,

                @Schema(description = "每个用户名每分钟允许的登录次数")
                @DefaultValue("3")
                Integer usernameLimitPerMinute,

                @Schema(description = "每个 IP 每小时允许的登录次数")
                @DefaultValue("20")
                Integer ipLimitPerHour
        ) {
        }

        @Schema(description = "Token 刷新接口限流配置")
        public record TokenRefreshRateLimitConfig(
                @Schema(description = "是否启用 Token 刷新接口限流")
                @DefaultValue("true")
                Boolean enabled,

                @Schema(description = "每个用户每分钟允许的刷新次数")
                @DefaultValue("10")
                Integer userLimitPerMinute,

                @Schema(description = "每个 IP 每分钟允许的刷新次数")
                @DefaultValue("30")
                Integer ipLimitPerMinute
        ) {
        }

        @Schema(description = "IP 级别限流配置")
        public record IpRateLimitConfig(
                @Schema(description = "是否启用 IP 级别限流")
                @DefaultValue("true")
                Boolean enabled,

                @Schema(description = "每个 IP 每分钟允许的请求次数（全局）")
                @DefaultValue("100")
                Integer globalLimitPerMinute,

                @Schema(description = "每个 IP 每小时允许的请求次数（全局）")
                @DefaultValue("1000")
                Integer globalLimitPerHour
        ) {
        }
    }
}
