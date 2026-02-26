package com.ysmjjsy.goya.component.admin.user.dto;

/**
 * <p>用户状态变更</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record UserStatusRequest(
        String tenantId,
        String status
) {
}
