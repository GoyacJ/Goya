package com.ysmjjsy.goya.component.security.authentication.dto;

import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.enums.MfaType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>短信登录请求</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(description = "短信登录请求")
public record SmsLoginRequest(
        String phoneNumber,
        String code,
        String tenantId,
        ClientTypeEnum clientType,
        String deviceId,
        MfaType mfaType,
        String mfaTarget,
        String totpSecret
) {
}
