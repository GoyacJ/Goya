package com.ysmjjsy.goya.component.framework.security.domain;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>策略效果：允许或拒绝。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum PolicyEffect implements CodeEnum<String> {

    ALLOW("ALLOW", "Allow"),
    DENY("DENY", "Deny");

    private final String code;
    private final String label;
}
