package com.ysmjjsy.goya.component.framework.security.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>权限变更类型</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Getter
@AllArgsConstructor
public enum PermissionChangeType {

    CREATE("CREATE", "Create"),
    UPDATE("UPDATE", "Update"),
    DELETE("DELETE", "Delete"),
    PUBLISH("PUBLISH", "Publish");

    private final String code;
    private final String label;
}
