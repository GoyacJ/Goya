package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>区间谓词。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class BetweenPredicate implements RangeExpression {
    @Serial
    private static final long serialVersionUID = -5034745595466086593L;

    private String field;
    private Value start;
    private Value end;
}
