package com.ysmjjsy.goya.component.framework.security.dsl;

import lombok.Data;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>时间值。</p>
 *
 * @author goya
 * @since 2026/1/31 10:20
 */
@Data
public class DateTimeValue implements Value {
    @Serial
    private static final long serialVersionUID = -6042829175564253571L;

    private LocalDateTime value;

    @Override
    public Object getValue() {
        return value;
    }
}
