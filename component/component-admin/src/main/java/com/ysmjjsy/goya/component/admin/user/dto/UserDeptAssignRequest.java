package com.ysmjjsy.goya.component.admin.user.dto;

import java.util.List;

/**
 * <p>用户部门绑定请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record UserDeptAssignRequest(
        String tenantId,
        List<String> deptIds
) {
}
