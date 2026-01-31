package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>二元表达式。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class BinaryExpression implements RangeExpression {
    @Serial
    private static final long serialVersionUID = -3797122606232226342L;

    private RangeExpression left;
    private LogicalOperator operator;
    private RangeExpression right;
}
