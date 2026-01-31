package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>数值。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class NumberValue implements Value {
    @Serial
    private static final long serialVersionUID = -7932450280098030740L;

    private Number value;

    @Override
    public Object getValue() {
        return value;
    }
}
