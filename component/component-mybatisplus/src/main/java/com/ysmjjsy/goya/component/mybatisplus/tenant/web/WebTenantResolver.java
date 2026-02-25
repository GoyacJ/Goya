package com.ysmjjsy.goya.component.mybatisplus.tenant.web;

import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * <p>Web 租户解析器</p>
 *
 * <p>优先从 TenantContext 读取，其次从请求头读取。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public class WebTenantResolver implements TenantResolver {

    /**
     * 解析租户 ID。
     *
     * @return tenantId
     */
    @Override
    public String resolveTenantId() {
        String tenantId = TenantContext.get().tenantId();
        if (StringUtils.hasText(tenantId)) {
            return tenantId;
        }

        HttpServletRequest request = WebUtils.getRequest();
        if (request == null) {
            return null;
        }

        GoyaContext goyaContext = SpringContext.getBeanOrNull(GoyaContext.class);
        if (goyaContext != null) {
            String resolvedTenant = goyaContext.currentTenant();
            if (StringUtils.hasText(resolvedTenant)) {
                return resolvedTenant.trim();
            }
        }

        if (StringUtils.hasText(WebUtils.getBearerToken(request))) {
            return null;
        }

        String header = WebUtils.getTenantId(request);
        return StringUtils.hasText(header) ? header.trim() : null;
    }
}
