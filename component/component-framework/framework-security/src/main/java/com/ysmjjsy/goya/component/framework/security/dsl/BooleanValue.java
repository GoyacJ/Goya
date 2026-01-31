package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;

/**
 * <p>布尔值。</p>
 *
 * @author goya
 * @since 2026/1/31 10:20
 */
@Data
public class BooleanValue implements Value {
    @Serial
    private static final long serialVersionUID = 6888885374618119778L;

    private Boolean value;

    @Override
    public Boolean getValue() {
        return value;
    }
}
