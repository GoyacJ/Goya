package com.ysmjjsy.goya.component.framework.security.dsl;

/**
 * <p>范围 DSL 解析器。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface RangeDslParser {

    /**
     * 解析 DSL 为范围表达式。
     *
     * @param dsl DSL 字符串
     * @return 范围表达式
     */
    RangeExpression parse(String dsl);
}
