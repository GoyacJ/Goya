package com.ysmjjsy.goya.component.admin.role.dto;

import java.util.List;

/**
 * <p>角色菜单绑定请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record RoleMenuAssignRequest(
        String tenantId,
        List<String> menuIds
) {
}
