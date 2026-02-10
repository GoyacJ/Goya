package com.ysmjjsy.goya.component.security.authentication.dto;

import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.enums.MfaType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>密码登录请求</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(description = "密码登录请求")
public record PasswordLoginRequest(
        String username,
        String password,
        String tenantId,
        ClientTypeEnum clientType,
        String deviceId,
        MfaType mfaType,
        String mfaTarget,
        String totpSecret,
        String captchaIdentity,
        CaptchaCategoryEnum captchaCategory,
        String captchaCharacters
) {
}
