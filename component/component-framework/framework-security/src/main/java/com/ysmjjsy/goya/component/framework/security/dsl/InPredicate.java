package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;
import java.util.List;

/**
 * <p>集合匹配谓词。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class InPredicate implements RangeExpression {
    @Serial
    private static final long serialVersionUID = -2613242076721615237L;

    private String field;
    private boolean negated;
    private List<Value> values;
}
