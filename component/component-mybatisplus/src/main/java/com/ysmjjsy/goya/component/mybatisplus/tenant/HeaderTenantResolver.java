package com.ysmjjsy.goya.component.mybatisplus.tenant;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContextValue;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantResolver;

/**
 * <p>默认租户解析器（Web Header）</p>
 * 解析优先级：
 * <ol>
 *   <li>若当前线程已存在 {@link TenantContext}，直接返回其中的 tenantId</li>
 *   <li>若存在 Web 请求上下文，从 Header 读取 tenantId</li>
 * </ol>
 *
 * <p><b>注意：</b>
 * 本实现不做“缺失即拒绝”，拒绝策略由注入点（Filter/Aspect）根据 requireTenant 控制。
 * @author goya
 * @since 2026/1/28 22:08
 */
public class HeaderTenantResolver implements TenantResolver {

    @Override
    public String resolveTenantId() {
        TenantContextValue existing = TenantContext.get();
        if (existing != null) {
            return existing.tenantId();
        }
        return WebUtils.getHeader(WebUtils.getRequest(), DefaultConst.X_TENANT_ID);
    }
}