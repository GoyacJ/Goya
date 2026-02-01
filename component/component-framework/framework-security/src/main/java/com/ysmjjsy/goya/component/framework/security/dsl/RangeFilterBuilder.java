package com.ysmjjsy.goya.component.framework.security.dsl;

/**
 * <p>范围过滤器构建器。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface RangeFilterBuilder {

    /**
     * 将范围表达式构建为过滤器。
     *
     * @param expression 范围表达式
     * @param context 构建上下文
     * @return 范围过滤器
     */
    RangeFilter build(RangeExpression expression, RangeFilterContext context);
}
