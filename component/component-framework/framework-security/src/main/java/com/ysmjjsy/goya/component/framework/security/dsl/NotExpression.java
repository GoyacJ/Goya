package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>非表达式。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class NotExpression implements RangeExpression {
    @Serial
    private static final long serialVersionUID = -7804128008711234895L;

    private RangeExpression expression;
}
