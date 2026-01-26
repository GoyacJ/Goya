package com.ysmjjsy.goya.component.security.core.service;

import com.ysmjjsy.goya.component.security.core.domain.SecurityTenant;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;

/**
 * <p>租户服务SPI</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface ITenantService {

    /**
     * 解析请求中的租户ID
     *
     * @param request HTTP请求
     * @return tenantId，解析失败返回null
     */
    @Nullable
    String resolveTenantId(HttpServletRequest request);

    /**
     * 加载租户配置
     *
     * @param tenantId 租户ID
     * @return 租户配置
     */
    @Nullable
    SecurityTenant loadTenant(String tenantId);
}
