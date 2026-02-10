package com.ysmjjsy.goya.component.security.authentication.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * <p>认证响应体</p>
 *
 * @param status          状态
 * @param preAuthCode     预认证码
 * @param mfaChallengeId  MFA挑战ID
 * @param expiresInSecond 过期秒数
 * @param grantType       扩展授权类型
 * @param sid             会话ID
 * @param message         提示文案
 * @author goya
 * @since 2026/2/10
 */
@Schema(description = "认证响应体")
public record AuthResult(
        String status,
        String preAuthCode,
        String mfaChallengeId,
        Long expiresInSecond,
        String grantType,
        String sid,
        String message
) implements Serializable {

    public static AuthResult preAuthIssued(String preAuthCode,
                                           long expiresInSecond,
                                           String grantType,
                                           String sid) {
        return new AuthResult("PRE_AUTH_CODE_ISSUED", preAuthCode, null, expiresInSecond, grantType, sid, "认证成功");
    }

    public static AuthResult mfaRequired(String mfaChallengeId,
                                         long expiresInSecond,
                                         String sid,
                                         String reason) {
        return new AuthResult("MFA_REQUIRED", null, mfaChallengeId, expiresInSecond, null, sid, reason);
    }
}
