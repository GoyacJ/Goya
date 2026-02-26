package com.ysmjjsy.goya.component.admin.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>用户状态</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Getter
@AllArgsConstructor
public enum UserStatusEnum implements CodeEnum<String> {

    ENABLED("ENABLED", "启用"),

    DISABLED("DISABLED", "禁用"),

    LOCKED("LOCKED", "锁定");

    private final String code;
    private final String label;
}
