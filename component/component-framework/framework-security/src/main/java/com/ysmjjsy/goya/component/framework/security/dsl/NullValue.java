package com.ysmjjsy.goya.component.framework.security.dsl;

import java.io.Serial;

/**
 * <p>空值。</p>
 *
 * @author goya
 * @since 2026/1/31 10:20
 */
public class NullValue implements Value {
    @Serial
    private static final long serialVersionUID = -4049347890823386908L;

    @Override
    public Object getValue() {
        return null;
    }
}
