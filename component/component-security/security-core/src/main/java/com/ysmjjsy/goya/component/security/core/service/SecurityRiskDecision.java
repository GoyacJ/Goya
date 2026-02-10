package com.ysmjjsy.goya.component.security.core.service;

import com.ysmjjsy.goya.component.security.core.enums.MfaType;

/**
 * <p>登录风险决策</p>
 *
 * @param requireMfa 是否需要MFA
 * @param mfaType    MFA类型
 * @param reason     决策原因
 * @author goya
 * @since 2026/2/10
 */
public record SecurityRiskDecision(
        boolean requireMfa,
        MfaType mfaType,
        String reason
) {

    public static SecurityRiskDecision allow() {
        return new SecurityRiskDecision(false, null, "ALLOW");
    }

    public static SecurityRiskDecision require(MfaType mfaType, String reason) {
        return new SecurityRiskDecision(true, mfaType, reason);
    }
}
