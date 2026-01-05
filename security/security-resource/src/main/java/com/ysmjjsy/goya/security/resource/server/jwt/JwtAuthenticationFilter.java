package com.ysmjjsy.goya.security.resource.server.jwt;

import com.ysmjjsy.goya.component.common.tenant.TenantContext;
import com.ysmjjsy.goya.security.core.constants.IStandardClaimNamesConstants;
import com.ysmjjsy.goya.security.resource.server.configuration.properties.SecurityResourceProperties;
import com.ysmjjsy.goya.security.resource.server.dpop.ResourceServerDPoPValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * <p>JWT认证过滤器</p>
 * <p>在Spring Security的OAuth2资源服务器过滤器链之后执行</p>
 * <p>用于额外的验证：DPoP Proof验证、多租户上下文设置等</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final ResourceServerDPoPValidator dPoPValidator;
    private final SecurityResourceProperties resourceProperties;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. 获取当前认证对象（应该已经是JwtAuthenticationToken）
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();

            // 2. 验证DPoP Proof（如果Token是DPoP-bound）
            try {
                dPoPValidator.validateIfRequired(jwt, request);
            } catch (Exception e) {
                log.warn("[Goya] |- security [resource] DPoP validation failed: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"invalid_dpop_proof\",\"error_description\":\"" + e.getMessage() + "\"}");
                return;
            }

            // 3. 设置多租户上下文（如果启用）
            String tenantId = null;
            if (resourceProperties.multiTenant() != null && resourceProperties.multiTenant().enabled()) {
                try {
                    // 从JWT中提取tenantId
                    tenantId = jwt.getClaimAsString(IStandardClaimNamesConstants.TENANT_ID);
                    
                    // 如果JWT中没有tenantId，使用默认值
                    if (StringUtils.isBlank(tenantId)) {
                        tenantId = resourceProperties.multiTenant().defaultTenantId();
                    }
                    
                    // 设置到TenantContext
                    if (StringUtils.isNotBlank(tenantId)) {
                        TenantContext.setTenantId(tenantId);
                        log.debug("[Goya] |- security [resource] TenantContext set for tenantId: {}", tenantId);
                    }
                } catch (Exception e) {
                    log.warn("[Goya] |- security [resource] Failed to set tenant context: {}", e.getMessage());
                }
            }

            try {
                // 4. 继续过滤器链
                filterChain.doFilter(request, response);
            } finally {
                // 5. 清理多租户上下文（确保线程安全）
                if (tenantId != null) {
                    TenantContext.clear();
                    log.trace("[Goya] |- security [resource] TenantContext cleared.");
                }
            }
        } else {
            // 非JWT认证，直接通过
            filterChain.doFilter(request, response);
        }
    }
}

