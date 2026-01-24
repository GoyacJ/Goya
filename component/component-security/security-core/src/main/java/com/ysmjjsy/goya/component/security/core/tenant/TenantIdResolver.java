package com.ysmjjsy.goya.component.security.core.tenant;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;

/**
 * <p>租户ID解析器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface TenantIdResolver {

    /**
     * 解析请求中的租户ID
     *
     * @param request HTTP请求
     * @return tenantId
     */
    @Nullable
    String resolveTenantId(HttpServletRequest request);
}
