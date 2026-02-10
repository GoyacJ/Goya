package com.ysmjjsy.goya.component.security.oauth2.service;

import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import java.util.List;

/**
 * <p>OAuth2Authorization 服务SPI</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface IOAuth2AuthorizationService {

    void save(OAuth2Authorization authorization);

    void remove(OAuth2Authorization authorization);

    OAuth2Authorization findById(String id);

    OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType);

    /**
     * 根据 principalName（用户名）查找所有授权记录
     * <p>用于 Token 撤销场景，查找用户的所有活跃授权</p>
     *
     * @param principalName 用户名（principal name）
     * @return 授权记录列表
     */
    @Nullable
    default List<OAuth2Authorization> findByPrincipalName(String principalName) {
        // 默认实现返回空列表，子类可以覆盖此方法
        return List.of();
    }

    /**
     * 根据 principalName 和设备ID查找授权记录
     * <p>用于撤销指定设备的 Token</p>
     *
     * @param principalName 用户名
     * @param deviceId       设备ID
     * @return 授权记录列表
     */
    @Nullable
    default List<OAuth2Authorization> findByPrincipalNameAndDevice(String principalName, String deviceId) {
        // 默认实现返回空列表，子类可以覆盖此方法
        return List.of();
    }
}
