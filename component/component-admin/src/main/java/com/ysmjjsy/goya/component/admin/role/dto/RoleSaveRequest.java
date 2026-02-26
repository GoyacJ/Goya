package com.ysmjjsy.goya.component.admin.role.dto;

/**
 * <p>角色创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record RoleSaveRequest(
        String tenantId,
        String roleCode,
        String roleName,
        String dataScope,
        String status,
        String remark
) {
}
