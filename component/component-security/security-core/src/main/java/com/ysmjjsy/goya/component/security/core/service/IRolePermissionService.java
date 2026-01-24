package com.ysmjjsy.goya.component.security.core.service;

import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * <p>角色权限服务SPI</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface IRolePermissionService {

    /**
     * 读取用户角色
     *
     * @param userId 用户ID
     * @return 角色集合
     */
    Set<String> findRolesByUserId(String userId);

    /**
     * 读取用户权限
     *
     * @param userId 用户ID
     * @return 权限集合
     */
    Set<String> findPermissionsByUserId(String userId);

    /**
     * 可选：根据租户和用户获取角色（多租户场景）
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 角色集合
     */
    default @Nullable Set<String> findRolesByTenant(String tenantId, String userId) {
        return null;
    }
}
