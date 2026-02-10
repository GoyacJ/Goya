package com.ysmjjsy.goya.component.security.core.context;

import com.google.common.collect.Sets;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaStringUtils;
import com.ysmjjsy.goya.component.framework.core.context.GoyaUser;
import com.ysmjjsy.goya.component.framework.servlet.context.AbstractGoyaContext;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.domain.SecurityGrantedAuthority;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.service.ITenantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>安全上下文实现</p>
 * <p>提供获取当前用户和租户信息的功能</p>
 *
 * @author goya
 * @since 2025/12/31 11:47
 */
@Slf4j
public class GoyaSecurityContext extends AbstractGoyaContext {

    private final SecurityCoreProperties securityCoreProperties;
    private final SecurityUserManager securityUserManager;
    private final ObjectProvider<ITenantService> tenantServiceProvider;
    private final ObjectProvider<JwtDecoder> jwtDecoderProvider;

    public GoyaSecurityContext(ServerProperties serverProperties,
                               SecurityCoreProperties securityCoreProperties,
                               SecurityUserManager securityUserManager,
                               ObjectProvider<ITenantService> tenantServiceProvider,
                               ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        super(serverProperties);
        this.securityCoreProperties = securityCoreProperties;
        this.securityUserManager = securityUserManager;
        this.tenantServiceProvider = tenantServiceProvider;
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    @Override
    public String getAuthServiceUri() {
        if (StringUtils.isNotBlank(securityCoreProperties.authServiceUri())) {
            return securityCoreProperties.authServiceUri();
        }
        return super.getAuthServiceUri();
    }

    @Override
    public String getAuthServiceName() {
        if (StringUtils.isNotBlank(securityCoreProperties.authServiceName())) {
            return securityCoreProperties.authServiceName();
        }
        return super.getAuthServiceName();
    }

    @Override
    public GoyaUser currentUser() {
        GoyaUser goyaUser = resolveUserFromSecurityContext();
        if (goyaUser != null) {
            return goyaUser;
        }

        HttpServletRequest request = WebUtils.getRequest();
        if (request != null) {
            return currentUser(request);
        }
        return null;
    }

    @Override
    public GoyaUser currentUser(HttpServletRequest request) {
        GoyaUser goyaUser = resolveUserFromSecurityContext();
        if (goyaUser != null) {
            return goyaUser;
        }

        String bearerToken = WebUtils.getBearerToken(request);
        if (StringUtils.isBlank(bearerToken)) {
            return null;
        }
        return currentUser(bearerToken);
    }

    @Override
    public GoyaUser currentUser(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }

        JwtDecoder jwtDecoder = jwtDecoderProvider.getIfAvailable();
        if (jwtDecoder == null) {
            return null;
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);
            return mapJwtToSecurityUser(jwt);
        } catch (JwtException ex) {
            log.debug("[Goya] |- security [core] decode jwt failed in currentUser(token): {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public String currentTenant() {
        String tenantFromAuthentication = resolveTenantFromAuthentication();
        if (StringUtils.isNotBlank(tenantFromAuthentication)) {
            return tenantFromAuthentication;
        }

        HttpServletRequest request = WebUtils.getRequest();
        if (request != null
                && securityCoreProperties.tenant().enabled()
                && (!isAuthenticatedRequest()
                || securityCoreProperties.tenant().allowHeaderFallbackWhenAuthenticated())) {
            ITenantService tenantService = tenantServiceProvider.getIfAvailable();
            if (tenantService != null) {
                String tenantId = tenantService.resolveTenantId(request);
                if (StringUtils.isNotBlank(tenantId)) {
                    return tenantId;
                }
            }

            String fromHeader = request.getHeader(securityCoreProperties.tenant().tenantHeader());
            if (StringUtils.isNotBlank(fromHeader)) {
                return fromHeader;
            }
        }

        return securityCoreProperties.tenant().defaultTenantId();
    }

    @Nullable
    private GoyaUser resolveUserFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            return mapJwtToSecurityUser(jwtAuthenticationToken.getToken());
        }
        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            return mapAttributesToSecurityUser(
                    bearerTokenAuthentication.getTokenAttributes(),
                    bearerTokenAuthentication.getName()
            );
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser;
        }
        if (principal instanceof GoyaUser goyaUser) {
            return goyaUser;
        }
        if (principal instanceof Jwt jwt) {
            return mapJwtToSecurityUser(jwt);
        }
        if (principal instanceof OAuth2AuthenticatedPrincipal oauth2AuthenticatedPrincipal) {
            return mapAttributesToSecurityUser(
                    oauth2AuthenticatedPrincipal.getAttributes(),
                    oauth2AuthenticatedPrincipal.getName()
            );
        }
        if (principal instanceof Map<?, ?> principalMap) {
            Map<String, Object> attributes = new LinkedHashMap<>();
            principalMap.forEach((key, value) -> attributes.put(String.valueOf(key), value));
            return mapAttributesToSecurityUser(attributes, authentication.getName());
        }
        if (principal instanceof String username
                && StringUtils.isNotBlank(username)
                && !"anonymousUser".equalsIgnoreCase(username)) {
            try {
                return securityUserManager.findUserByUsername(username);
            } catch (Exception ex) {
                log.debug("[Goya] |- security [core] load user from username failed: {}", ex.getMessage());
            }
        }

        return null;
    }

    @Nullable
    private String resolveTenantFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
            String tenant = jwtAuthenticationToken.getToken()
                    .getClaimAsString(securityCoreProperties.tenant().tenantClaim());
            if (StringUtils.isNotBlank(tenant)) {
                return tenant;
            }
        }

        if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
            Object tenant = bearerTokenAuthentication
                    .getTokenAttributes()
                    .get(securityCoreProperties.tenant().tenantClaim());
            if (tenant != null && StringUtils.isNotBlank(String.valueOf(tenant))) {
                return String.valueOf(tenant);
            }
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser && StringUtils.isNotBlank(securityUser.getTenantId())) {
            return securityUser.getTenantId();
        }
        if (principal instanceof OAuth2AuthenticatedPrincipal oauth2AuthenticatedPrincipal) {
            Object tenant = oauth2AuthenticatedPrincipal.getAttribute(securityCoreProperties.tenant().tenantClaim());
            if (tenant != null && StringUtils.isNotBlank(String.valueOf(tenant))) {
                return String.valueOf(tenant);
            }
        }
        if (principal instanceof Map<?, ?> principalMap) {
            Object tenant = principalMap.get(securityCoreProperties.tenant().tenantClaim());
            if (tenant != null && StringUtils.isNotBlank(String.valueOf(tenant))) {
                return String.valueOf(tenant);
            }
        }

        return null;
    }

    private boolean isAuthenticatedRequest() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equalsIgnoreCase(authentication.getName());
    }

    private SecurityUser mapJwtToSecurityUser(JwtClaimAccessor claimAccessor) {
        String userId = resolveUserId(claimAccessor);
        String username = resolveClaimString(claimAccessor, securityCoreProperties.claims().username());
        if (StringUtils.isBlank(username)) {
            username = userId;
        }

        Set<String> roles = toStringSet(resolveClaimObject(claimAccessor, securityCoreProperties.claims().roles()));
        Set<String> permissions = toStringSet(resolveClaimObject(claimAccessor, securityCoreProperties.claims().authorities()));

        Set<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();
        permissions.forEach(permission -> grantedAuthorities.add(new SecurityGrantedAuthority(permission)));
        roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .forEach(role -> grantedAuthorities.add(new SecurityGrantedAuthority(role)));

        return SecurityUser.builder()
                .userId(userId)
                .username(username)
                .password(StringUtils.EMPTY)
                .openId(resolveClaimString(claimAccessor, securityCoreProperties.claims().openId()))
                .tenantId(resolveClaimString(claimAccessor, securityCoreProperties.tenant().tenantClaim()))
                .roles(roles)
                .authorities(grantedAuthorities)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    private SecurityUser mapAttributesToSecurityUser(Map<String, Object> attributes, String principalName) {
        if (attributes == null || attributes.isEmpty()) {
            return null;
        }

        String userId = resolveUserId(attributes, principalName);
        String username = resolveClaimString(attributes, securityCoreProperties.claims().username());
        if (StringUtils.isBlank(username)) {
            username = StringUtils.defaultIfBlank(principalName, userId);
        }

        Set<String> roles = toStringSet(attributes.get(securityCoreProperties.claims().roles()));
        Set<String> permissions = toStringSet(attributes.get(securityCoreProperties.claims().authorities()));

        Set<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();
        permissions.forEach(permission -> grantedAuthorities.add(new SecurityGrantedAuthority(permission)));
        roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .forEach(role -> grantedAuthorities.add(new SecurityGrantedAuthority(role)));

        return SecurityUser.builder()
                .userId(userId)
                .username(username)
                .password(StringUtils.EMPTY)
                .openId(resolveClaimString(attributes, securityCoreProperties.claims().openId()))
                .tenantId(resolveClaimString(attributes, securityCoreProperties.tenant().tenantClaim()))
                .roles(roles)
                .authorities(grantedAuthorities)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    private String resolveUserId(JwtClaimAccessor claimAccessor) {
        String configuredClaimName = securityCoreProperties.claims().userId();
        String configuredValue = resolveClaimString(claimAccessor, configuredClaimName);
        if (StringUtils.isNotBlank(configuredValue)) {
            return configuredValue;
        }

        String subject = claimAccessor.getSubject();
        if (StringUtils.isNotBlank(subject)) {
            return subject;
        }

        return "anonymous";
    }

    private String resolveUserId(Map<String, Object> attributes, String principalName) {
        String configuredClaimName = securityCoreProperties.claims().userId();
        String configuredValue = resolveClaimString(attributes, configuredClaimName);
        if (StringUtils.isNotBlank(configuredValue)) {
            return configuredValue;
        }

        Object sub = attributes.get("sub");
        if (sub != null && StringUtils.isNotBlank(String.valueOf(sub))) {
            return String.valueOf(sub);
        }

        if (StringUtils.isNotBlank(principalName)) {
            return principalName;
        }
        return "anonymous";
    }

    @Nullable
    private static Object resolveClaimObject(JwtClaimAccessor claimAccessor, String claimName) {
        if (StringUtils.isBlank(claimName)) {
            return null;
        }
        return claimAccessor.getClaims().get(claimName);
    }

    private static String resolveClaimString(JwtClaimAccessor claimAccessor, String claimName) {
        Object value = resolveClaimObject(claimAccessor, claimName);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    @Nullable
    private static String resolveClaimString(Map<String, Object> attributes, String claimName) {
        if (attributes == null || StringUtils.isBlank(claimName)) {
            return null;
        }
        Object value = attributes.get(claimName);
        if (value == null) {
            return null;
        }
        return String.valueOf(value);
    }

    private static Set<String> toStringSet(@Nullable Object rawValue) {
        if (rawValue == null) {
            return Collections.emptySet();
        }

        if (rawValue instanceof Collection<?> collection) {
            Set<String> set = new LinkedHashSet<>();
            for (Object item : collection) {
                if (item != null) {
                    set.add(String.valueOf(item));
                }
            }
            return set;
        }

        if (rawValue instanceof String value) {
            if (GoyaStringUtils.isBlank(value)) {
                return Collections.emptySet();
            }
            String[] parts = value.split(",");
            Set<String> set = new LinkedHashSet<>();
            for (String part : parts) {
                if (StringUtils.isNotBlank(part)) {
                    set.add(part.trim());
                }
            }
            return set;
        }

        return Sets.newHashSet(String.valueOf(rawValue));
    }
}
