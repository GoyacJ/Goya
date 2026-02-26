package com.ysmjjsy.goya.component.admin.user.dto;

/**
 * <p>用户密码变更</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record UserPasswordRequest(
        String tenantId,
        String password
) {
}
