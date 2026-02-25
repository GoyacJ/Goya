package com.ysmjjsy.goya.component.security.authorization.filter;

import com.ysmjjsy.goya.component.security.authorization.configuration.properties.SecurityAuthorizationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * <p>租户Claim校验过滤器</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class TenantClaimValidationFilter extends OncePerRequestFilter {

    private final SecurityAuthorizationProperties securityAuthorizationProperties;

    public TenantClaimValidationFilter(SecurityAuthorizationProperties securityAuthorizationProperties) {
        this.securityAuthorizationProperties = securityAuthorizationProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestTenant = request.getHeader(securityAuthorizationProperties.tenantHeader());
        String tokenTenant = resolveTenantFromAuthentication(authentication, securityAuthorizationProperties.tenantClaim());

        if (StringUtils.isNotBlank(requestTenant)
                && StringUtils.isNotBlank(tokenTenant)
                && !StringUtils.equals(requestTenant, tokenTenant)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant mismatch");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveTenantFromAuthentication(Authentication authentication, String claimName) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken().getClaimAsString(claimName);
        }

        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Object value = bearerTokenAuthentication.getTokenAttributes().get(claimName);
            return value == null ? null : String.valueOf(value);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2AuthenticatedPrincipal oAuth2AuthenticatedPrincipal) {
            Object value = oAuth2AuthenticatedPrincipal.getAttribute(claimName);
            return value == null ? null : String.valueOf(value);
        }

        if (principal instanceof Map<?, ?> principalMap) {
            Object value = principalMap.get(claimName);
            return value == null ? null : String.valueOf(value);
        }

        return null;
    }
}
