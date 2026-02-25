package com.ysmjjsy.goya.component.security.core.service;

import com.ysmjjsy.goya.component.security.core.domain.SecuritySessionRevocationResult;

/**
 * <p>安全会话生命周期服务</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public interface SecuritySessionLifecycleService {

    /**
     * 注销当前会话（优先按 sid）。
     *
     * @param sid      会话 sid
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param clientId 客户端ID
     * @return 撤销结果
     */
    SecuritySessionRevocationResult logoutCurrent(String sid, String tenantId, String userId, String clientId);

    /**
     * 按 sid 撤销会话。
     *
     * @param sid 会话 sid
     * @return 撤销结果
     */
    SecuritySessionRevocationResult revokeBySid(String sid);

    /**
     * 按用户撤销会话。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 撤销结果
     */
    SecuritySessionRevocationResult revokeByUser(String tenantId, String userId);

    /**
     * 按用户+客户端撤销会话。
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @param clientId 客户端ID
     * @return 撤销结果
     */
    SecuritySessionRevocationResult revokeByUserAndClient(String tenantId, String userId, String clientId);
}
