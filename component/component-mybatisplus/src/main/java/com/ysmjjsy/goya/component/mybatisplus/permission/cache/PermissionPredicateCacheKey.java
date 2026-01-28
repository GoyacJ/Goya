package com.ysmjjsy.goya.component.mybatisplus.permission.cache;

import java.io.Serializable;

/**
 * <p>权限谓词 L2 缓存 key</p>
 * <p>
 * key 必须包含 version，以便规则变更后立即切换 key 实现快速生效。
 *
 * @param tenantId 租户 ID
 * @param subjectId 主体 ID
 * @param resource 资源
 * @param version 版本号
 * @author goya
 * @since 2026/1/28 23:04
 */
public record PermissionPredicateCacheKey(
        String tenantId,
        String subjectId,
        String resource,
        long version
) implements Serializable {
}