package com.ysmjjsy.goya.component.security.authentication.configuration.properties;

import com.ysmjjsy.goya.component.security.core.constants.SecurityConst;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.enums.MfaType;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Set;

/**
 * <p>认证模块配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(defaultValue = "Security 认证配置")
@ConfigurationProperties(prefix = SecurityConst.PROPERTY_PLATFORM_SECURITY_AUTHENTICATION)
public record SecurityAuthenticationProperties(

        @Schema(defaultValue = "是否启用认证模块")
        @DefaultValue("true")
        boolean enabled,

        @Schema(defaultValue = "登录页地址")
        @DefaultValue("/security/login")
        String loginPage,

        @Schema(defaultValue = "预认证码有效期")
        @DefaultValue("PT60S")
        Duration preAuthCodeTtl,

        @Schema(defaultValue = "MFA挑战有效期")
        @DefaultValue("PT300S")
        Duration mfaChallengeTtl,

        @Schema(defaultValue = "登录失败窗口期")
        @DefaultValue("PT15M")
        Duration loginAttemptWindow,

        @Schema(defaultValue = "登录失败阈值")
        @DefaultValue("5")
        int loginAttemptThreshold,

        @Schema(defaultValue = "临时锁定时长")
        @DefaultValue("PT15M")
        Duration lockDuration,

        @Schema(defaultValue = "是否启用验证码")
        @DefaultValue("false")
        boolean captchaEnabled,

        @Schema(defaultValue = "是否启用MFA")
        @DefaultValue("true")
        boolean mfaEnabled,

        @Schema(defaultValue = "默认MFA类型")
        @DefaultValue("SMS")
        MfaType defaultMfaType,

        @Schema(defaultValue = "不可信设备是否强制MFA")
        @DefaultValue("true")
        boolean requireMfaForUntrustedDevice,

        @Schema(defaultValue = "强制MFA客户端类型")
        @DefaultValue("MOBILE_APP,MINIPROGRAM")
        Set<ClientTypeEnum> forceMfaClientTypes,

        @Schema(defaultValue = "设备标识请求头")
        @DefaultValue("X-Device-Id")
        String deviceIdHeader,

        @Schema(defaultValue = "预认证授权类型")
        @DefaultValue("urn:goya:grant-type:pre-auth-code")
        String tokenExchangeGrantType,

        @Schema(defaultValue = "预认证码前缀")
        @DefaultValue("pac_")
        String preAuthCodePrefix,

        @Schema(defaultValue = "认证模块缓存前缀")
        @DefaultValue("security-auth")
        String cachePrefix
) {

    public Set<ClientTypeEnum> forceMfaClientTypesOrEmpty() {
        return forceMfaClientTypes == null ? Set.of() : forceMfaClientTypes;
    }
}
