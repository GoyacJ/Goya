package com.ysmjjsy.goya.component.mybatisplus.tenant.web;

import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantResolver;
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
        String header = WebUtils.getTenantId(WebUtils.getRequest());
        return StringUtils.hasText(header) ? header.trim() : null;
    }
}
