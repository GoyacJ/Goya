package com.ysmjjsy.goya.component.admin.policy.dto;

/**
 * <p>资源创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record ResourceSaveRequest(
        String tenantId,
        String resourceHashcode,
        String resourceCode,
        String resourceParentCode,
        String resourceParentCodes,
        String resourceOperType,
        String resourceName,
        String resourceType,
        String resourceDesc,
        String resourceOwner
) {
}
