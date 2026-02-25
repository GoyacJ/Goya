package com.ysmjjsy.goya.component.security.oauth2.filter;

import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.core.domain.SecuritySessionRevocationResult;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.service.SecuritySessionLifecycleService;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>OIDC 登出撤销联动过滤器</p>
 *
 * @author goya
 * @since 2026/2/25
 */
@Slf4j
public class OidcLogoutRevocationFilter extends OncePerRequestFilter {

    private final SecuritySessionLifecycleService securitySessionLifecycleService;
    private final SecurityOAuth2Properties securityOAuth2Properties;

    public OidcLogoutRevocationFilter(SecuritySessionLifecycleService securitySessionLifecycleService,
                                      SecurityOAuth2Properties securityOAuth2Properties) {
        this.securitySessionLifecycleService = securitySessionLifecycleService;
        this.securityOAuth2Properties = securityOAuth2Properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!isOidcLogoutRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String sid = resolveSid(authentication);
            String tenantId = resolveTenant(authentication);
            String userId = resolveUserId(authentication);
            String clientId = resolveClientId(authentication);
            SecuritySessionRevocationResult result = securitySessionLifecycleService.logoutCurrent(sid, tenantId, userId, clientId);
            log.debug("[Goya] |- security [oauth2] oidc logout revoke sid={}, tenant={}, user={}, client={}, matched={}, revoked={}",
                    sid,
                    tenantId,
                    userId,
                    clientId,
                    result.matchedCount(),
                    result.revokedCount());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOidcLogoutRequest(HttpServletRequest request) {
        SecurityOAuth2Properties.Endpoints endpoints = securityOAuth2Properties.endpoints();
        if (endpoints == null || StringUtils.isBlank(endpoints.oidcLogoutEndpoint())) {
            return false;
        }
        String expectedPath = endpoints.oidcLogoutEndpoint();
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (StringUtils.isNotBlank(contextPath) && StringUtils.startsWith(requestPath, contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        return StringUtils.equals(requestPath, expectedPath);
    }

    private String resolveSid(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> detailMap) {
            Object sid = detailMap.get(StandardClaimNamesConst.SID);
            if (sid != null && StringUtils.isNotBlank(String.valueOf(sid))) {
                return String.valueOf(sid);
            }
        }
        Map<String, Object> attributes = resolveTokenAttributes(authentication);
        Object sid = attributes.get(StandardClaimNamesConst.SID);
        if (sid != null) {
            return String.valueOf(sid);
        }
        return null;
    }

    private String resolveTenant(Authentication authentication) {
        if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getTenantId();
        }
        Map<String, Object> attributes = resolveTokenAttributes(authentication);
        Object tenant = attributes.get(StandardClaimNamesConst.TENANT_ID);
        if (tenant != null) {
            return String.valueOf(tenant);
        }
        return null;
    }

    private String resolveUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return StringUtils.defaultIfBlank(securityUser.getUserId(), securityUser.getUsername());
        }
        Map<String, Object> attributes = resolveTokenAttributes(authentication);
        Object user = attributes.get("sub");
        if (user != null) {
            return String.valueOf(user);
        }
        return authentication.getName();
    }

    private String resolveClientId(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> detailMap) {
            Object clientId = detailMap.get(StandardClaimNamesConst.CLIENT_ID);
            if (clientId != null && StringUtils.isNotBlank(String.valueOf(clientId))) {
                return String.valueOf(clientId);
            }
        }
        Map<String, Object> attributes = resolveTokenAttributes(authentication);
        Object clientId = attributes.get(StandardClaimNamesConst.CLIENT_ID);
        if (clientId != null) {
            return String.valueOf(clientId);
        }
        return null;
    }

    private Map<String, Object> resolveTokenAttributes(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return jwtAuthenticationToken.getToken().getClaims();
        }
        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            return bearerTokenAuthentication.getTokenAttributes();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Map<?, ?> principalMap) {
            Map<String, Object> attributes = new LinkedHashMap<>();
            principalMap.forEach((key, value) -> attributes.put(String.valueOf(key), value));
            return attributes;
        }
        return Collections.emptyMap();
    }
}
