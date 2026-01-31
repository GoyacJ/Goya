package com.ysmjjsy.goya.component.framework.security.decision;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * <p>带解释的鉴权决策。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DecisionExplain extends Decision {

    @Serial
    private static final long serialVersionUID = 7465555600131746931L;

    /**
     * 允许的策略
     */
    private List<String> allowPolicyIds;

    /**
     * 拒绝的策略
     */
    private List<String> denyPolicyIds;

    /**
     * 行级范围 DSL
     */
    private List<String> appliedRanges;

    /**
     * 显式继承
     */
    private boolean inheritApplied;

    /**
     * taceId
     */
    private String traceId;

    public static DecisionExplain denyExplain(String reason) {
        DecisionExplain decision = new DecisionExplain();
        decision.setDecisionType(DecisionType.DENY);
        decision.setReason(reason);
        return decision;
    }
}
