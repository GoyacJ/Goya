package com.ysmjjsy.goya.component.admin.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>菜单类型</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum implements CodeEnum<String> {

    DIRECTORY("DIRECTORY", "目录"),

    MENU("MENU", "菜单"),

    BUTTON("BUTTON", "按钮");

    private final String code;
    private final String label;
}
