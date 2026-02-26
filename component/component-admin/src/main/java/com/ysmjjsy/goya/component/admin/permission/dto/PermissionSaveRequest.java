package com.ysmjjsy.goya.component.admin.permission.dto;

/**
 * <p>权限创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record PermissionSaveRequest(
        String tenantId,
        String permissionCode,
        String permissionName,
        String resourceType,
        String resourceCode,
        String actionCode,
        String status,
        String remark
) {
}
