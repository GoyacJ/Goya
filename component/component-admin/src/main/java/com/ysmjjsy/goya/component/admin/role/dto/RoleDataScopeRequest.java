package com.ysmjjsy.goya.component.admin.role.dto;

import java.util.List;

/**
 * <p>角色数据范围配置</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record RoleDataScopeRequest(
        String tenantId,
        String dataScope,
        List<String> deptIds
) {
}
