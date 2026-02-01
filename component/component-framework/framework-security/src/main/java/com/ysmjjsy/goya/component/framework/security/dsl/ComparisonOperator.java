package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>比较运算符。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum ComparisonOperator {

    EQ("=", "Equals"),
    NE("!=", "NotEquals"),
    GT(">", "GreaterThan"),
    GTE(">=", "GreaterThanOrEqual"),
    LT("<", "LessThan"),
    LTE("<=", "LessThanOrEqual"),
    LIKE("LIKE", "Like");

    private final String code;
    private final String label;
}
