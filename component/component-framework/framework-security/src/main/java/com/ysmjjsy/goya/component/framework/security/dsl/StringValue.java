package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>字符串值。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class StringValue implements Value {
    @Serial
    private static final long serialVersionUID = 2596040216625149442L;

    private String value;

    @Override
    public Object getValue() {
        return value;
    }
}
