package com.ysmjjsy.goya.component.admin.dept.dto;

/**
 * <p>部门创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record DeptSaveRequest(
        String tenantId,
        String deptCode,
        String deptName,
        String parentId,
        String leader,
        String phone,
        String email,
        Integer sort,
        String status
) {
}
