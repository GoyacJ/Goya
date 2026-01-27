package com.ysmjjsy.goya.component.framework.servlet.tenant;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.context.TenantContext;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * <p>多租户过滤器</p>
 *
 * @author goya
 * @since 2025/10/15 09:40
 */
public class MultiTenantFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String tenantId = WebUtils.getTenantId(request);
        TenantContext.setTenantId(StringUtils.isBlank(tenantId) ? DefaultConst.DEFAULT_TENANT_ID : tenantId);

        filterChain.doFilter(servletRequest, servletResponse);
        TenantContext.clear();
    }

    @Override
    public void destroy() {
        TenantContext.clear();
    }
}