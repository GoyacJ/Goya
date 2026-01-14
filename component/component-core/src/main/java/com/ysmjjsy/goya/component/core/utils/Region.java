package com.ysmjjsy.goya.component.core.utils;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 17:10
 */

public record Region(
        String country,
        String area,
        String province,
        String city,
        String isp,
        String raw
) {

    public static final Region EMPTY = new Region(
            "", "", "", "", "", ""
    );

    public boolean isEmpty() {
        return this == EMPTY || raw == null || raw.isEmpty();
    }
}