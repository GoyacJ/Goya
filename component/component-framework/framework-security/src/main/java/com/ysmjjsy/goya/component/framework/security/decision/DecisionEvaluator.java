package com.ysmjjsy.goya.component.framework.security.decision;

import com.ysmjjsy.goya.component.framework.security.domain.Policy;

import java.util.List;

/**
 * <p>策略合并与决策评估器。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface DecisionEvaluator {

    /**
     * 合并策略并生成决策结果。
     *
     * @param policies 策略列表
     * @param context 决策上下文
     * @return 决策结果
     */
    Decision merge(List<Policy> policies, DecisionContext context);

    /**
     * 合并策略并生成可解释的决策结果。
     *
     * @param policies 策略列表
     * @param context 决策上下文
     * @return 决策结果（包含解释信息）
     */
    DecisionExplain mergeWithExplain(List<Policy> policies, DecisionContext context);
}
