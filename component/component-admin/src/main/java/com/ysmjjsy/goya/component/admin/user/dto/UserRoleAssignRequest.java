package com.ysmjjsy.goya.component.admin.user.dto;

import java.util.List;

/**
 * <p>用户角色绑定请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record UserRoleAssignRequest(
        String tenantId,
        List<String> roleIds
) {
}
