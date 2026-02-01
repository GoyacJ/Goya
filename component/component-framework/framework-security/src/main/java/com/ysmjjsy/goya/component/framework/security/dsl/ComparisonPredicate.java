package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>比较谓词。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class ComparisonPredicate implements RangeExpression {
    @Serial
    private static final long serialVersionUID = 4166467029332995957L;

    private String field;
    private ComparisonOperator operator;
    private Value value;
}
