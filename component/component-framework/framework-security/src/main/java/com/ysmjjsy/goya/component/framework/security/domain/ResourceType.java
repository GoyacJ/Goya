package com.ysmjjsy.goya.component.framework.security.domain;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>鉴权资源类型。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum ResourceType implements CodeEnum<String> {

    DB("DB", "Database"),
    TABLE("TABLE", "Table"),
    FIELD("FIELD", "Field"),
    ROW("ROW", "Row"),
    API("API", "API"),
    FILE("FILE", "File"),
    MENU("MENU", "Menu"),
    CUSTOM("CUSTOM", "Custom");

    private final String code;
    private final String label;
}
