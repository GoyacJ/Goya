package com.ysmjjsy.goya.component.admin.support;

import com.ysmjjsy.goya.component.admin.configuration.properties.AdminProperties;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * <p>租户解析支持</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public class AdminTenantSupport {

    private final AdminProperties adminProperties;

    public AdminTenantSupport(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    public String requiredTenantId(String preferredTenantId) {
        String tenantId = resolveTenantId(preferredTenantId);
        return StringUtils.defaultIfBlank(tenantId, adminProperties.defaultTenantId());
    }

    public String resolveTenantId(String preferredTenantId) {
        if (StringUtils.isNotBlank(preferredTenantId)) {
            return preferredTenantId;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof SecurityUser securityUser
                    && StringUtils.isNotBlank(securityUser.getTenantId())) {
                return securityUser.getTenantId();
            }
            if (authentication instanceof JwtAuthenticationToken jwtAuthenticationToken) {
                String tenantId = jwtAuthenticationToken.getToken().getClaimAsString(StandardClaimNamesConst.TENANT_ID);
                if (StringUtils.isNotBlank(tenantId)) {
                    return tenantId;
                }
            }
            if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
                Object tenant = bearerTokenAuthentication.getTokenAttributes().get(StandardClaimNamesConst.TENANT_ID);
                if (tenant != null && StringUtils.isNotBlank(String.valueOf(tenant))) {
                    return String.valueOf(tenant);
                }
            }
            if (authentication.getPrincipal() instanceof Map<?, ?> principalMap) {
                Object tenant = principalMap.get(StandardClaimNamesConst.TENANT_ID);
                if (tenant != null && StringUtils.isNotBlank(String.valueOf(tenant))) {
                    return String.valueOf(tenant);
                }
            }
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            if (request != null) {
                String tenantHeader = request.getHeader(adminProperties.tenantHeader());
                if (StringUtils.isNotBlank(tenantHeader)) {
                    return tenantHeader;
                }
                String tenantQuery = request.getParameter("tenantId");
                if (StringUtils.isNotBlank(tenantQuery)) {
                    return tenantQuery;
                }
            }
        }

        return adminProperties.defaultTenantId();
    }
}
