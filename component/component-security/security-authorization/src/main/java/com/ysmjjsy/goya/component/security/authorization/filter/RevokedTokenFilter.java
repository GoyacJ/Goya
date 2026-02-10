package com.ysmjjsy.goya.component.security.authorization.filter;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.security.authorization.configuration.properties.SecurityAuthorizationProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>吊销令牌过滤器</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class RevokedTokenFilter extends OncePerRequestFilter {

    private final CacheService cacheService;
    private final SecurityAuthorizationProperties securityAuthorizationProperties;

    public RevokedTokenFilter(CacheService cacheService,
                              SecurityAuthorizationProperties securityAuthorizationProperties) {
        this.cacheService = cacheService;
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

        for (String tokenIdentifier : resolveTokenIdentifiers(authentication)) {
            if (cacheService.exists(securityAuthorizationProperties.revokedCacheName(), tokenIdentifier)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token revoked");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Set<String> resolveTokenIdentifiers(Authentication authentication) {
        Set<String> identifiers = new LinkedHashSet<>();
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            if (jwtAuthenticationToken.getToken().getId() != null) {
                identifiers.add(jwtAuthenticationToken.getToken().getId());
            }
            if (jwtAuthenticationToken.getToken().getTokenValue() != null) {
                identifiers.add(jwtAuthenticationToken.getToken().getTokenValue());
            }
            return identifiers;
        }

        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Object jti = bearerTokenAuthentication.getTokenAttributes().get("jti");
            if (jti != null) {
                identifiers.add(String.valueOf(jti));
            }
            if (bearerTokenAuthentication.getToken() != null
                    && bearerTokenAuthentication.getToken().getTokenValue() != null) {
                identifiers.add(bearerTokenAuthentication.getToken().getTokenValue());
            }
            return identifiers;
        }

        return identifiers;
    }
}
