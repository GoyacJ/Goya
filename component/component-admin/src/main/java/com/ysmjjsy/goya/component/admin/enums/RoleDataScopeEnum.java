package com.ysmjjsy.goya.component.admin.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>角色数据范围</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Getter
@AllArgsConstructor
public enum RoleDataScopeEnum implements CodeEnum<String> {

    ALL("ALL", "全部"),

    SELF("SELF", "仅自己"),

    DEPT("DEPT", "本部门"),

    DEPT_AND_CHILD("DEPT_AND_CHILD", "部门及子部门"),

    CUSTOM("CUSTOM", "自定义"),

    NONE("NONE", "无");

    private final String code;
    private final String label;
}
