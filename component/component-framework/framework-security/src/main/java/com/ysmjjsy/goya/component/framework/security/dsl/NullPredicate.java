package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>空值谓词。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class NullPredicate implements RangeExpression {
    @Serial
    private static final long serialVersionUID = -6118198114642520435L;

    private String field;
    private boolean negated;
}
