package com.ysmjjsy.goya.component.security.oauth2.tenant;

import com.ysmjjsy.goya.component.framework.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p>租户Issuer上下文过滤器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@RequiredArgsConstructor
public class TenantAuthorizationServerContextFilter extends OncePerRequestFilter {

    private final TenantIssuerResolver tenantIssuerResolver;
    private final AuthorizationServerSettings authorizationServerSettings;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = TenantContext.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            tenantId = (String) request.getAttribute(TenantRequestAttributes.ATTR_TENANT_ID);
        }

        if (StringUtils.isBlank(tenantId)) {
            filterChain.doFilter(request, response);
            return;
        }

        String issuer = tenantIssuerResolver.resolveIssuer(request, tenantId);
        AuthorizationServerContextHolder.setContext(
                new DefaultAuthorizationServerContext(authorizationServerSettings, issuer));

        try {
            filterChain.doFilter(request, response);
        } finally {
            AuthorizationServerContextHolder.resetContext();
        }
    }
}
