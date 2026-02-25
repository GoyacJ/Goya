package com.ysmjjsy.goya.component.security.authentication.dto;

/**
 * <p>会话撤销范围</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public enum SessionRevokeScope {
    CURRENT_SESSION,
    ALL_SESSIONS,
    BY_CLIENT;

    public static SessionRevokeScope resolve(SessionRevokeScope scope) {
        return scope == null ? CURRENT_SESSION : scope;
    }
}
