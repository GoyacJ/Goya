package com.ysmjjsy.goya.component.framework.security.decision;

import com.ysmjjsy.goya.component.framework.security.domain.Policy;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyEffect;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>默认策略合并与决策评估实现。</p>
 *
 * <p>规则：拒绝优先，默认拒绝。</p>
 *
 * @author goya
 * @since 2026/1/31 10:20
 */
public class DefaultDecisionEvaluator implements DecisionEvaluator {

    @Override
    public Decision merge(List<Policy> policies, DecisionContext context) {
        Decision decision = new Decision();
        DecisionType decisionType = decide(policies);
        decision.setDecisionType(decisionType);
        decision.setReason(decisionType == DecisionType.DENY ? "拒绝优先或无可用策略" : "允许");
        return decision;
    }

    @Override
    public DecisionExplain mergeWithExplain(List<Policy> policies, DecisionContext context) {
        DecisionExplain decision = new DecisionExplain();
        List<String> allowIds = new ArrayList<>();
        List<String> denyIds = new ArrayList<>();

        if (policies != null) {
            for (Policy policy : policies) {
                if (policy == null || policy.getPolicyEffect() == null) {
                    continue;
                }
                if (policy.getPolicyEffect() == PolicyEffect.DENY) {
                    denyIds.add(policy.getPolicyId());
                } else if (policy.getPolicyEffect() == PolicyEffect.ALLOW) {
                    allowIds.add(policy.getPolicyId());
                }
            }
        }

        DecisionType decisionType;
        if (!denyIds.isEmpty()) {
            decisionType = DecisionType.DENY;
        } else if (allowIds.isEmpty()) {
            decisionType = DecisionType.DENY;
        } else {
            decisionType = DecisionType.ALLOW;
        }
        decision.setDecisionType(decisionType);
        decision.setReason(decisionType == DecisionType.DENY ? "拒绝优先或无可用策略" : "允许");
        decision.setAllowPolicyIds(allowIds);
        decision.setDenyPolicyIds(denyIds);
        return decision;
    }

    /**
     * 决策
     * @param policies 授权策略
     * @return 结果
     */
    private DecisionType decide(List<Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            return DecisionType.DENY;
        }

        boolean hasAllow = false;
        for (Policy p : policies) {
            PolicyEffect e = (p == null) ? null : p.getPolicyEffect();
            if (e == PolicyEffect.DENY) {
                return DecisionType.DENY;
            }
            if (e == PolicyEffect.ALLOW) {
                hasAllow = true;
            }
        }
        return hasAllow ? DecisionType.ALLOW : DecisionType.DENY;
    }
}
