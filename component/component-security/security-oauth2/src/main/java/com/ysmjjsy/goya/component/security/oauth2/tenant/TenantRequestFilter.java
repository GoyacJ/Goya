package com.ysmjjsy.goya.component.security.oauth2.tenant;

import com.ysmjjsy.goya.component.framework.tenant.TenantContext;
import com.ysmjjsy.goya.component.security.core.tenant.TenantIdResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p>租户上下文过滤器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@RequiredArgsConstructor
public class TenantRequestFilter extends OncePerRequestFilter {

    private final TenantIdResolver tenantIdResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = tenantIdResolver.resolveTenantId(request);
        if (StringUtils.isBlank(tenantId)) {
            filterChain.doFilter(request, response);
            return;
        }

        String originalUri = request.getRequestURI();
        String originalUrl = request.getRequestURL().toString();
        String strippedUri = stripTenantPrefix(originalUri, tenantId);

        request.setAttribute(TenantRequestAttributes.ATTR_TENANT_ID, tenantId);
        request.setAttribute(TenantRequestAttributes.ATTR_ORIGINAL_URI, originalUri);
        request.setAttribute(TenantRequestAttributes.ATTR_ORIGINAL_URL, originalUrl);

        TenantContext.setTenantId(tenantId);

        try {
            TenantPathRequestWrapper wrapper =
                    new TenantPathRequestWrapper(request, originalUri, originalUrl, strippedUri);
            filterChain.doFilter(wrapper, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String stripTenantPrefix(String requestUri, String tenantId) {
        String prefix = "/t/" + tenantId;
        if (requestUri.startsWith(prefix)) {
            String stripped = requestUri.substring(prefix.length());
            return StringUtils.isNotBlank(stripped) ? stripped : "/";
        }
        return requestUri;
    }
}
