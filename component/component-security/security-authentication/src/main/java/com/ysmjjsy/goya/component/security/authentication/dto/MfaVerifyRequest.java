package com.ysmjjsy.goya.component.security.authentication.dto;

import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>MFA校验请求</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(description = "MFA校验请求")
public record MfaVerifyRequest(
        String challengeId,
        String code,
        String totpSecret,
        ClientTypeEnum clientType,
        String deviceId,
        Boolean rememberDevice
) {
}
