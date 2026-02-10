package com.ysmjjsy.goya.component.security.authentication.dto;

import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.enums.MfaType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * <p>MFA挑战请求</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Schema(description = "MFA挑战请求")
public record MfaChallengeRequest(
        String userId,
        String tenantId,
        ClientTypeEnum clientType,
        String deviceId,
        MfaType mfaType,
        String target,
        String sid,
        String totpSecret
) {
}
