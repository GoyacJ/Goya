package com.ysmjjsy.goya.component.security.core.service;

/**
 * <p>登录风险评估扩展点</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@FunctionalInterface
public interface LoginRiskEvaluator {

    /**
     * 评估登录风险。
     *
     * @param context 登录上下文
     * @return 风险决策
     */
    SecurityRiskDecision evaluate(SecurityRiskContext context);
}
