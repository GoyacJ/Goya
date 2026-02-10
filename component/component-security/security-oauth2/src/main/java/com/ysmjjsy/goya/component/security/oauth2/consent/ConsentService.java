package com.ysmjjsy.goya.component.security.oauth2.consent;

import java.util.Set;

/**
 * <p>用户授权同意服务接口</p>
 * <p>存储用户授权同意记录，检查用户是否已授权</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface ConsentService {

    /**
     * 检查用户是否已授权
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @param scopes 授权范围
     * @return true如果已授权，false如果未授权
     */
    boolean isConsentGiven(String userId, String clientId, Set<String> scopes);

    /**
     * 保存用户授权同意记录
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @param scopes 授权范围
     */
    void saveConsent(String userId, String clientId, Set<String> scopes);

    /**
     * 撤销用户授权同意
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     */
    void revokeConsent(String userId, String clientId);

    /**
     * 获取用户已授权的范围
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @return 已授权的范围集合
     */
    Set<String> getConsentedScopes(String userId, String clientId);

    /**
     * 查询用户的所有授权记录
     *
     * @param userId 用户ID
     * @return 授权记录列表（客户端ID -> 授权范围）
     */
    java.util.Map<String, Set<String>> findAllConsents(String userId);
}

