package com.ysmjjsy.goya.component.security.core.context;

import com.ysmjjsy.goya.component.framework.core.context.GoyaUser;
import com.ysmjjsy.goya.component.framework.servlet.context.AbstractGoyaContext;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

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
    private final ObjectProvider<JwtDecoder> jwtDecoderProvider;

    public GoyaSecurityContext(ServerProperties serverProperties,
                                SecurityCoreProperties securityCoreProperties,
                                SecurityUserManager securityUserManager,
                                ObjectProvider<JwtDecoder> jwtDecoderProvider) {
        super(serverProperties);
        this.securityCoreProperties = securityCoreProperties;
        this.securityUserManager = securityUserManager;
        this.jwtDecoderProvider = jwtDecoderProvider;
    }

    @Override
    public String getAuthServiceUri() {
        return securityCoreProperties.authServiceUri();
    }

    @Override
    public String getAuthServiceName() {
        return securityCoreProperties.authServiceName();
    }

    /**
     * 获取当前认证用户
     * <p>从 SecurityContextHolder 中获取当前认证的用户信息</p>
     *
     * @return 当前用户，如果未认证则返回 null
     */
    @Override
    @Nullable
    public GoyaUser currentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();
            if (principal instanceof SecurityUser securityUser) {
                return securityUser;
            }

            // 如果 principal 是 Jwt，尝试从 JWT 中提取用户信息
            if (principal instanceof Jwt jwt) {
                return extractUserFromJwt(jwt);
            }

            log.debug("[Goya] |- security [core] Current principal is not SecurityUser: {}", principal.getClass().getName());
            return null;
        } catch (Exception e) {
            log.warn("[Goya] |- security [core] Failed to get current user", e);
            return null;
        }
    }

    /**
     * 从请求中获取当前用户
     * <p>优先从 SecurityContextHolder 获取，如果不存在则尝试从请求的 Authorization header 中提取 token 并解析</p>
     *
     * @param request HTTP 请求
     * @return 当前用户，如果未认证则返回 null
     */
    @Override
    @Nullable
    public GoyaUser currentUser(HttpServletRequest request) {
        // 1. 优先从 SecurityContextHolder 获取
        GoyaUser user = currentUser();
        if (user != null) {
            return user;
        }

        // 2. 尝试从请求的 Authorization header 中提取 token
        String token = extractBearerToken(request);
        if (StringUtils.isNotBlank(token)) {
            return currentUser(token);
        }

        return null;
    }

    /**
     * 从 JWT token 中解析用户信息
     * <p>解析 JWT token，提取用户标识（username 或 userId），然后通过 SecurityUserManager 查找用户</p>
     *
     * @param token JWT token 字符串
     * @return 用户信息，如果 token 无效或用户不存在则返回 null
     */
    @Override
    @Nullable
    public GoyaUser currentUser(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }

        JwtDecoder jwtDecoder = jwtDecoderProvider.getIfAvailable();
        if (jwtDecoder == null) {
            log.debug("[Goya] |- security [core] JwtDecoder not available, cannot parse token");
            return null;
        }

        try {
            // 1. 解析 JWT
            Jwt jwt = jwtDecoder.decode(token);

            // 2. 从 JWT 中提取用户信息
            return extractUserFromJwt(jwt);
        } catch (JwtException e) {
            log.debug("[Goya] |- security [core] Failed to decode token: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("[Goya] |- security [core] Failed to get user from token", e);
            return null;
        }
    }

    /**
     * 获取当前租户ID
     * <p>从 TenantContext 中获取当前租户ID</p>
     *
     * @return 租户ID，如果未设置则返回 null
     */
    @Override
    @Nullable
    public String currentTenant() {
        try {
            return TenantContext.getTenantId();
        } catch (Exception e) {
            log.debug("[Goya] |- security [core] Failed to get current tenant: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 JWT 中提取用户信息
     * <p>优先使用 userId claim，如果不存在则使用 sub (subject) claim 作为 username 查找用户</p>
     *
     * @param jwt JWT 对象
     * @return 用户信息，如果用户不存在则返回 null
     */
    @Nullable
    private SecurityUser extractUserFromJwt(Jwt jwt) {
        try {
            // 1. 优先使用 userId claim
            String userId = jwt.getClaimAsString("userId");
            if (StringUtils.isNotBlank(userId)) {
                return securityUserManager.findUserByUserId(userId);
            }

            // 2. 使用 sub (subject) claim 作为 username
            String username = jwt.getSubject();
            if (StringUtils.isNotBlank(username)) {
                return securityUserManager.findUserByUsername(username);
            }

            log.debug("[Goya] |- security [core] JWT does not contain userId or sub claim");
            return null;
        } catch (Exception e) {
            log.debug("[Goya] |- security [core] Failed to extract user from JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求的 Authorization header 中提取 Bearer token
     *
     * @param request HTTP 请求
     * @return Bearer token，如果不存在则返回 null
     */
    @Nullable
    private String extractBearerToken(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || !StringUtils.startsWithIgnoreCase(authHeader, "Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7).trim();
        return StringUtils.isNotBlank(token) ? token : null;
    }
}
