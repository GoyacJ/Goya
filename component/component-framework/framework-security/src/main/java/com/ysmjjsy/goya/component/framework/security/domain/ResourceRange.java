package com.ysmjjsy.goya.component.framework.security.domain;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>授权资源范围。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum ResourceRange implements CodeEnum<String> {

    /**
     * 仅自身资源。
     */
    SELF("SELF", "Self"),

    /**
     * 仅子级资源。
     */
    CHILDREN("CHILDREN", "Children"),

    /**
     * 自身与子级资源。
     */
    SELF_AND_CHILDREN("SELF_AND_CHILDREN", "SelfAndChildren");

    private final String code;
    private final String label;
}
