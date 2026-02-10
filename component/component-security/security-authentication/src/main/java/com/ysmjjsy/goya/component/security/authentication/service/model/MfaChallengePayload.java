package com.ysmjjsy.goya.component.security.authentication.service.model;

import com.ysmjjsy.goya.component.security.core.enums.MfaType;

/**
 * <p>MFA挑战负载</p>
 *
 * @param challengeId      挑战ID
 * @param mfaType          MFA类型
 * @param target           目标（手机号/邮箱）
 * @param totpSecret       TOTP密钥（可选）
 * @param preAuthCodeData  预认证负载
 * @param expireAtEpochSec 过期时间
 * @author goya
 * @since 2026/2/10
 */
public record MfaChallengePayload(
        String challengeId,
        MfaType mfaType,
        String target,
        String totpSecret,
        PreAuthCodePayload preAuthCodeData,
        long expireAtEpochSec
) {
}
