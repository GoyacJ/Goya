package com.ysmjjsy.goya.security.authentication.configuration.properties;

import com.ysmjjsy.goya.component.cache.annotation.PropertiesCache;
import com.ysmjjsy.goya.security.core.constants.ISecurityConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * <p>密码策略配置属性</p>
 * <p>配置企业级密码策略，包括复杂度要求、历史密码检查等</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Schema(description = "密码策略配置")
@PropertiesCache
@ConfigurationProperties(prefix = ISecurityConstants.PROPERTY_PLATFORM_SECURITY_AUTHENTICATION + ".password-policy")
public record PasswordPolicyProperties(

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

