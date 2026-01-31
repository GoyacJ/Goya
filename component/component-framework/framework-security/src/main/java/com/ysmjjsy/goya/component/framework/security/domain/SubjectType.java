package com.ysmjjsy.goya.component.framework.security.domain;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>鉴权主体类型。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum SubjectType implements CodeEnum<String> {

    /**
     * 用户。
     */
    USER("USER", "User"),

    /**
     * 角色。
     */
    ROLE("ROLE", "Role"),

    /**
     * 团队。
     */
    TEAM("TEAM", "Team"),

    /**
     * 组织或部门。
     */
    ORG("ORG", "Organization");

    private final String code;
    private final String label;
}
