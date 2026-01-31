package com.ysmjjsy.goya.component.framework.security.domain;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>策略范围。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum PolicyScope implements CodeEnum<String> {

    RESOURCE("RESOURCE", "Resource"),
    ROW("ROW", "Row"),
    COLUMN("COLUMN", "Column");

    private final String code;
    private final String label;
}
