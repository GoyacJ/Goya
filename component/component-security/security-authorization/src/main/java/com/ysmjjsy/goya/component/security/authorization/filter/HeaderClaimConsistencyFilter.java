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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>请求头与 Token Claim 一致性校验过滤器。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class HeaderClaimConsistencyFilter extends OncePerRequestFilter {

    private static final String CLAIM_GRANT_TYPE = "grant_type";

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
        boolean bypassMachineUserHeader = tokenIdentity.machineToken()
                && !securityAuthorizationProperties.requireUserHeaderForMachineToken();

        if (SecurityAuthorizationProperties.ConsistencyMode.STRICT == consistencyMode) {
            if (bypassMachineUserHeader) {
                if (StringUtils.isAnyBlank(requestTenant, tokenIdentity.tenantId())) {
                    deny(response, "machine token 缺少 tenant 身份信息");
                    return;
                }
                if (!StringUtils.equals(requestTenant, tokenIdentity.tenantId())) {
                    deny(response, "machine token tenant header 与 token 不一致");
                    return;
                }
            } else {
                if (StringUtils.isAnyBlank(requestTenant, requestUserId, tokenIdentity.tenantId(), tokenIdentity.userId())) {
                    deny(response, "header 或 token 关键身份信息缺失");
                    return;
                }
                if (!StringUtils.equals(requestTenant, tokenIdentity.tenantId())
                        || !StringUtils.equals(requestUserId, tokenIdentity.userId())) {
                    deny(response, "header 与 token 身份不一致");
                    return;
                }
            }
        } else {
            if (StringUtils.isNotBlank(requestTenant)
                    && StringUtils.isNotBlank(tokenIdentity.tenantId())
                    && !StringUtils.equals(requestTenant, tokenIdentity.tenantId())) {
                deny(response, "tenant header 与 token 不一致");
                return;
            }

            if (!bypassMachineUserHeader
                    && StringUtils.isNotBlank(requestUserId)
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
        String clientId = null;
        boolean machineToken = false;

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tenantClaim = securityAuthorizationProperties.tenantClaim();
            String userClaim = securityAuthorizationProperties.userClaim();
            tenantId = jwtAuthenticationToken.getToken().getClaimAsString(tenantClaim);
            userId = resolveJwtUser(jwtAuthenticationToken, userClaim);
            clientId = jwtAuthenticationToken.getToken().getClaimAsString(StandardClaimNamesConst.CLIENT_ID);
            machineToken = isMachineToken(
                    jwtAuthenticationToken.getToken().getClaimAsString(CLAIM_GRANT_TYPE),
                    clientId,
                    userId,
                    authentication.getName()
            );
        } else if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Map<String, Object> tokenAttributes = bearerTokenAuthentication.getTokenAttributes();
            tenantId = toStringValue(tokenAttributes.get(securityAuthorizationProperties.tenantClaim()));
            userId = resolveUserFromMap(tokenAttributes, securityAuthorizationProperties.userClaim());
            clientId = toStringValue(tokenAttributes.get(StandardClaimNamesConst.CLIENT_ID));
            machineToken = isMachineToken(
                    toStringValue(tokenAttributes.get(CLAIM_GRANT_TYPE)),
                    clientId,
                    userId,
                    authentication.getName()
            );
        } else {
            Object principal = authentication.getPrincipal();
            if (principal instanceof OAuth2AuthenticatedPrincipal oauth2Principal) {
                Map<String, Object> attributes = oauth2Principal.getAttributes();
                tenantId = toStringValue(attributes.get(securityAuthorizationProperties.tenantClaim()));
                userId = resolveUserFromMap(attributes, securityAuthorizationProperties.userClaim());
                clientId = toStringValue(attributes.get(StandardClaimNamesConst.CLIENT_ID));
                machineToken = isMachineToken(
                        toStringValue(attributes.get(CLAIM_GRANT_TYPE)),
                        clientId,
                        userId,
                        authentication.getName()
                );
            } else if (principal instanceof Map<?, ?> principalMap) {
                Map<String, Object> attributes = new LinkedHashMap<>();
                principalMap.forEach((key, value) -> attributes.put(String.valueOf(key), value));
                tenantId = toStringValue(attributes.get(securityAuthorizationProperties.tenantClaim()));
                userId = resolveUserFromMap(attributes, securityAuthorizationProperties.userClaim());
                clientId = toStringValue(attributes.get(StandardClaimNamesConst.CLIENT_ID));
                machineToken = isMachineToken(
                        toStringValue(attributes.get(CLAIM_GRANT_TYPE)),
                        clientId,
                        userId,
                        authentication.getName()
                );
            }
        }

        if (StringUtils.isBlank(userId) && StringUtils.isNotBlank(authentication.getName())) {
            userId = authentication.getName();
        }

        if (!machineToken) {
            machineToken = isMachineToken(null, clientId, userId, authentication.getName());
        }
        return new TokenIdentity(trim(tenantId), trim(userId), trim(clientId), machineToken);
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

    private boolean isMachineToken(String grantType, String clientId, String userId, String principalName) {
        if ("client_credentials".equalsIgnoreCase(StringUtils.defaultString(grantType))) {
            return true;
        }
        if (StringUtils.isBlank(clientId)) {
            return false;
        }
        if (StringUtils.equals(clientId, userId)) {
            return true;
        }
        return StringUtils.equals(clientId, principalName);
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

    private record TokenIdentity(String tenantId, String userId, String clientId, boolean machineToken) {
    }
}
