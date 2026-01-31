package com.ysmjjsy.goya.component.framework.security.decision;

/**
 * <p>策略评估引擎。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface PolicyEngine {

    /**
     * 评估策略并返回决策结果。
     *
     * @param context 决策上下文
     * @return 决策结果
     */
    Decision evaluate(DecisionContext context);

    /**
     * 评估策略并返回可解释的决策结果。
     *
     * @param context 决策上下文
     * @return 决策结果（包含解释信息）
     */
    DecisionExplain evaluateWithExplain(DecisionContext context);
}
