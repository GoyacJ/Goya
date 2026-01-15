package com.ysmjjsy.goya.component.security.authentication.captcha;

import com.ysmjjsy.goya.component.common.tenant.TenantContext;
import com.ysmjjsy.goya.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/4 11:49
 */
@Slf4j
@RequiredArgsConstructor
public class DynamicLoginCaptchaStrategy implements LoginCaptchaStrategy {

    private final SecurityAuthenticationProperties.CaptchaConfig captchaConfig;

    @Override
    public boolean shouldValidate(HttpServletRequest request, com.ysmjjsy.goya.security.core.enums.LoginTypeEnum loginType, @Nullable String tenantId) {
        // 1. 全局开关
        // 2. 根据登录方式判断是否默认启用
        boolean enabledByLoginType = switch (loginType) {
            case PASSWORD -> captchaConfig.password();
            case SMS -> captchaConfig.sms();
            case SOCIAL -> captchaConfig.social();
        };

        if (!enabledByLoginType) {
            return false;
        }

        // 3. 租户级差异化
        if (StringUtils.isNotBlank(tenantId)) {
            boolean captchaEnabled = TenantContext.isCaptchaEnabled(tenantId, loginType.getCode());
            if (!captchaEnabled) {
                return false;
            }
        }

        // 5. MFA 与验证码协同 如果 MFA 已开启，强制校验验证码
        return Boolean.TRUE.equals(request.getAttribute("mfa_required"));
    }
}
