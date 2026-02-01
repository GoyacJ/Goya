package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>逻辑运算符。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum LogicalOperator {

    AND("AND", "And"),
    OR("OR", "Or");

    private final String code;
    private final String label;
}
