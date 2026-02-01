package com.ysmjjsy.goya.component.framework.security.decision;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyEffect;

/**
 * <p>决策类型</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum DecisionType implements CodeEnum<String> {

    ALLOW("ALLOW", "Allow"),
    DENY("DENY", "Deny");

    private final String code;
    private final String label;

    /**
     * 从策略效果转换为决策类型。
     *
     * @param effect 策略效果
     * @return 决策类型
     */
    public static DecisionType fromPolicyEffect(PolicyEffect effect) {
        if (effect == null) {
            return null;
        }
        return effect == PolicyEffect.DENY ? DENY : ALLOW;
    }
}
