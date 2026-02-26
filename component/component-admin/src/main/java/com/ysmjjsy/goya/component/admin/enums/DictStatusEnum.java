package com.ysmjjsy.goya.component.admin.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>字典状态</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Getter
@AllArgsConstructor
public enum DictStatusEnum implements CodeEnum<String> {

    ENABLED("ENABLED", "启用"),

    DISABLED("DISABLED", "禁用");

    private final String code;
    private final String label;
}
