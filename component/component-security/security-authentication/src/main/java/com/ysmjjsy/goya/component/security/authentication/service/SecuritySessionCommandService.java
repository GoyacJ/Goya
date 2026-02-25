package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.security.authentication.dto.KickoutRequest;
import com.ysmjjsy.goya.component.security.authentication.dto.LogoutRequest;
import com.ysmjjsy.goya.component.security.authentication.dto.SessionRevokeScope;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.core.domain.SecuritySessionRevocationResult;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import com.ysmjjsy.goya.component.security.core.service.SecuritySessionLifecycleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>会话命令服务</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public class SecuritySessionCommandService {

    private static final Set<String> KICKOUT_AUTHORITIES = Set.of("ROLE_ADMIN", "security:kickout");

    private final ObjectProvider<SecuritySessionLifecycleService> securitySessionLifecycleServiceProvider;

    public SecuritySessionCommandService(ObjectProvider<SecuritySessionLifecycleService> securitySessionLifecycleServiceProvider) {
        this.securitySessionLifecycleServiceProvider = securitySessionLifecycleServiceProvider;
    }

    public Map<String, Object> logout(LogoutRequest request, HttpServletRequest servletRequest) {
        Authentication authentication = requiredAuthentication();
        SessionRevokeScope scope = SessionRevokeScope.resolve(request == null ? null : request.scope());

        String sid = firstNotBlank(request == null ? null : request.sid(), resolveSid(authentication));
        String tenantId = firstNotBlank(request == null ? null : request.tenantId(), resolveTenantId(authentication));
        String userId = firstNotBlank(request == null ? null : request.userId(), resolveUserId(authentication));
        String clientId = firstNotBlank(request == null ? null : request.clientId(), resolveClientId(authentication));

        SecuritySessionLifecycleService securitySessionLifecycleService = requiredSessionLifecycleService();
        SecuritySessionRevocationResult result;
        switch (scope) {
            case CURRENT_SESSION -> result = securitySessionLifecycleService.logoutCurrent(sid, tenantId, userId, clientId);
            case ALL_SESSIONS -> {
                requireNotBlank(tenantId, "tenantId 不能为空");
                requireNotBlank(userId, "userId 不能为空");
                result = securitySessionLifecycleService.revokeByUser(tenantId, userId);
            }
            case BY_CLIENT -> {
                requireNotBlank(tenantId, "tenantId 不能为空");
                requireNotBlank(userId, "userId 不能为空");
                requireNotBlank(clientId, "clientId 不能为空");
                result = securitySessionLifecycleService.revokeByUserAndClient(tenantId, userId, clientId);
            }
            default -> result = SecuritySessionRevocationResult.empty();
        }

        clearCurrentSession(servletRequest);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scope", scope.name());
        body.put("matched_count", result.matchedCount());
        body.put("revoked_count", result.revokedCount());
        body.put("sid", sid);
        body.put("tenant_id", tenantId);
        body.put("user_id", userId);
        body.put("client_id", clientId);
        if (request != null && StringUtils.isNotBlank(request.reason())) {
            body.put("reason", request.reason());
        }
        return body;
    }

    public Map<String, Object> kickout(KickoutRequest request) {
        if (request == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.AUTHENTICATION_FAILED, "请求不能为空");
        }
        Authentication authentication = requiredAuthentication();
        ensureKickoutAuthority(authentication);

        requireNotBlank(request.tenantId(), "tenantId 不能为空");
        requireNotBlank(request.userId(), "userId 不能为空");

        SecuritySessionLifecycleService securitySessionLifecycleService = requiredSessionLifecycleService();
        SecuritySessionRevocationResult result;
        if (StringUtils.isBlank(request.clientId())) {
            result = securitySessionLifecycleService.revokeByUser(request.tenantId(), request.userId());
        } else {
            result = securitySessionLifecycleService.revokeByUserAndClient(request.tenantId(), request.userId(), request.clientId());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scope", StringUtils.isBlank(request.clientId()) ? SessionRevokeScope.ALL_SESSIONS.name() : SessionRevokeScope.BY_CLIENT.name());
        body.put("matched_count", result.matchedCount());
        body.put("revoked_count", result.revokedCount());
        body.put("tenant_id", request.tenantId());
        body.put("user_id", request.userId());
        body.put("client_id", request.clientId());
        if (StringUtils.isNotBlank(request.reason())) {
            body.put("reason", request.reason());
        }
        return body;
    }

    private Authentication requiredAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new SecurityAuthenticationException(CommonErrorCode.UNAUTHORIZED, "未认证用户无法执行该操作");
        }
        return authentication;
    }

    private SecuritySessionLifecycleService requiredSessionLifecycleService() {
        SecuritySessionLifecycleService securitySessionLifecycleService = securitySessionLifecycleServiceProvider.getIfAvailable();
        if (securitySessionLifecycleService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE, "会话生命周期服务未配置");
        }
        return securitySessionLifecycleService;
    }

    private void ensureKickoutAuthority(Authentication authentication) {
        if (authentication.getAuthorities() == null) {
            throw new SecurityAuthenticationException(CommonErrorCode.FORBIDDEN, "无权限执行踢出操作");
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority != null && KICKOUT_AUTHORITIES.contains(authority.getAuthority())) {
                return;
            }
        }
        throw new SecurityAuthenticationException(CommonErrorCode.FORBIDDEN, "无权限执行踢出操作");
    }

    private void clearCurrentSession(HttpServletRequest servletRequest) {
        if (servletRequest != null) {
            HttpSession session = servletRequest.getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
        SecurityContextHolder.clearContext();
    }

    private String resolveSid(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> detailMap) {
            Object sid = detailMap.get(StandardClaimNamesConst.SID);
            if (sid != null) {
                return String.valueOf(sid);
            }
        }
        Map<String, Object> attrs = resolveTokenAttributes(authentication);
        Object sid = attrs.get(StandardClaimNamesConst.SID);
        return sid == null ? null : String.valueOf(sid);
    }

    private String resolveTenantId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getTenantId();
        }
        Map<String, Object> attrs = resolveTokenAttributes(authentication);
        Object tenant = attrs.get(StandardClaimNamesConst.TENANT_ID);
        return tenant == null ? null : String.valueOf(tenant);
    }

    private String resolveUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return StringUtils.defaultIfBlank(securityUser.getUserId(), securityUser.getUsername());
        }
        Map<String, Object> attrs = resolveTokenAttributes(authentication);
        Object sub = attrs.get("sub");
        if (sub != null) {
            return String.valueOf(sub);
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
        Map<String, Object> attrs = resolveTokenAttributes(authentication);
        Object clientId = attrs.get(StandardClaimNamesConst.CLIENT_ID);
        return clientId == null ? null : String.valueOf(clientId);
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

    private String firstNotBlank(String preferred, String fallback) {
        if (StringUtils.isNotBlank(preferred)) {
            return preferred;
        }
        return fallback;
    }

    private void requireNotBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new SecurityAuthenticationException(SecurityErrorCode.AUTHENTICATION_FAILED, message);
        }
    }
}
