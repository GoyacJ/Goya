package com.ysmjjsy.goya.component.admin.policy.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>策略创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record PolicySaveRequest(
        String tenantId,
        String subjectType,
        String subjectId,
        String resourceType,
        String resourceCode,
        String actionCode,
        String policyEffect,
        String policyScope,
        String rangeDsl,
        List<String> allowColumns,
        List<String> denyColumns,
        Boolean inheritFlag,
        String resourceRange,
        Boolean neverExpire,
        LocalDateTime expireTime
) {
}
