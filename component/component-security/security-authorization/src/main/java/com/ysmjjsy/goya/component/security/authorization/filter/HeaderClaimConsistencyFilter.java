package com.ysmjjsy.goya.component.security.authorization.filter;

import com.ysmjjsy.goya.component.security.authorization.configuration.properties.SecurityAuthorizationProperties;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * <p>请求头与 Token Claim 一致性校验过滤器。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class HeaderClaimConsistencyFilter extends OncePerRequestFilter {

    private final SecurityAuthorizationProperties securityAuthorizationProperties;

    public HeaderClaimConsistencyFilter(SecurityAuthorizationProperties securityAuthorizationProperties) {
        this.securityAuthorizationProperties = securityAuthorizationProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        SecurityAuthorizationProperties.ConsistencyMode consistencyMode = securityAuthorizationProperties.consistencyMode();
        if (SecurityAuthorizationProperties.ConsistencyMode.OFF == consistencyMode) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestTenant = trim(request.getHeader(securityAuthorizationProperties.tenantHeader()));
        String requestUserId = trim(request.getHeader(securityAuthorizationProperties.userHeader()));
        TokenIdentity tokenIdentity = resolveTokenIdentity(authentication);

        if (SecurityAuthorizationProperties.ConsistencyMode.STRICT == consistencyMode) {
            if (StringUtils.isAnyBlank(requestTenant, requestUserId, tokenIdentity.tenantId(), tokenIdentity.userId())) {
                deny(response, "header 或 token 关键身份信息缺失");
                return;
            }
            if (!StringUtils.equals(requestTenant, tokenIdentity.tenantId())
                    || !StringUtils.equals(requestUserId, tokenIdentity.userId())) {
                deny(response, "header 与 token 身份不一致");
                return;
            }
        } else {
            if (StringUtils.isNotBlank(requestTenant)
                    && StringUtils.isNotBlank(tokenIdentity.tenantId())
                    && !StringUtils.equals(requestTenant, tokenIdentity.tenantId())) {
                deny(response, "tenant header 与 token 不一致");
                return;
            }
            if (StringUtils.isNotBlank(requestUserId)
                    && StringUtils.isNotBlank(tokenIdentity.userId())
                    && !StringUtils.equals(requestUserId, tokenIdentity.userId())) {
                deny(response, "user header 与 token 不一致");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private TokenIdentity resolveTokenIdentity(Authentication authentication) {
        String tenantId = null;
        String userId = null;

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tenantClaim = securityAuthorizationProperties.tenantClaim();
            String userClaim = securityAuthorizationProperties.userClaim();
            tenantId = jwtAuthenticationToken.getToken().getClaimAsString(tenantClaim);
            userId = resolveJwtUser(jwtAuthenticationToken, userClaim);
        } else if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Map<String, Object> tokenAttributes = bearerTokenAuthentication.getTokenAttributes();
            tenantId = toStringValue(tokenAttributes.get(securityAuthorizationProperties.tenantClaim()));
            userId = resolveUserFromMap(tokenAttributes, securityAuthorizationProperties.userClaim());
        } else {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2AuthenticatedPrincipal oauth2Principal) {
                tenantId = toStringValue(oauth2Principal.getAttribute(securityAuthorizationProperties.tenantClaim()));
                userId = resolveUserFromMap(oauth2Principal.getAttributes(), securityAuthorizationProperties.userClaim());
            } else if (principal instanceof Map<?, ?> principalMap) {
                tenantId = toStringValue(principalMap.get(securityAuthorizationProperties.tenantClaim()));
                userId = resolveUserFromMap(principalMap, securityAuthorizationProperties.userClaim());
            }
        }

        if (StringUtils.isBlank(userId) && StringUtils.isNotBlank(authentication.getName())) {
            userId = authentication.getName();
        }

        return new TokenIdentity(trim(tenantId), trim(userId));
    }

    private String resolveJwtUser(JwtAuthenticationToken jwtAuthenticationToken, String userClaim) {
        String fromConfiguredClaim;
        if (StandardClaimNamesConst.SUB.equalsIgnoreCase(userClaim)) {
            fromConfiguredClaim = jwtAuthenticationToken.getToken().getSubject();
        } else {
            fromConfiguredClaim = jwtAuthenticationToken.getToken().getClaimAsString(userClaim);
        }
        if (StringUtils.isNotBlank(fromConfiguredClaim)) {
            return fromConfiguredClaim;
        }
        return jwtAuthenticationToken.getToken().getSubject();
    }

    private String resolveUserFromMap(Map<?, ?> source, String userClaim) {
        if (source == null) {
            return null;
        }
        Object value = source.get(userClaim);
        String resolved = toStringValue(value);
        if (StringUtils.isNotBlank(resolved)) {
            return resolved;
        }
        return toStringValue(source.get(StandardClaimNamesConst.SUB));
    }

    private String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String trim(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private void deny(HttpServletResponse response, String reason) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                SecurityErrorCode.SUBJECT_MISMATCH.code() + ": " + reason);
    }

    private record TokenIdentity(String tenantId, String userId) {
    }
}
