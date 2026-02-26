package com.ysmjjsy.goya.component.admin.user.dto;

/**
 * <p>用户创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record UserSaveRequest(
        String tenantId,
        String username,
        String password,
        String nickname,
        String phoneNumber,
        String email,
        String avatar,
        String openId,
        String status
) {
}
