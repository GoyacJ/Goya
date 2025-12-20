package com.ysmjjsy.goya.security.core.service;

import com.ysmjjsy.goya.security.core.domain.SecurityGrantedAuthority;

import java.util.Set;

/**
 * <p>客户端操作基础接口</p>
 *
 * @author goya
 * @since 2025/10/10 14:34
 */
public interface IOAuth2ClientService {

    /**
     * 根据客户端ID获取客户端权限
     *
     * @param clientId 客户端ID
     * @return 客户端权限集合
     */
    Set<SecurityGrantedAuthority> findAuthoritiesById(String clientId);
}
