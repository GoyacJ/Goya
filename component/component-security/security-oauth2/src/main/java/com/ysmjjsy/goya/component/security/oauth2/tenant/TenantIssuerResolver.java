package com.ysmjjsy.goya.component.security.oauth2.tenant;

import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>租户Issuer解析器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface TenantIssuerResolver {

    /**
     * 解析当前请求的issuer
     *
     * @param request  HTTP请求
     * @param tenantId 租户ID
     * @return issuer
     */
    String resolveIssuer(HttpServletRequest request, String tenantId);
}
