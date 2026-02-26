package com.ysmjjsy.goya.component.admin.security;

import com.ysmjjsy.goya.component.admin.configuration.properties.AdminProperties;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileStore;
import com.ysmjjsy.goya.component.security.core.domain.SecurityTenant;
import com.ysmjjsy.goya.component.security.core.service.ITenantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

/**
 * <p>租户 SPI 默认实现</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Service
@RequiredArgsConstructor
@ConditionalOnMissingBean(ITenantService.class)
public class AdminTenantService implements ITenantService {

    private final AdminProperties adminProperties;
    private final AdminTenantSupport adminTenantSupport;
    private final TenantProfileStore tenantProfileStore;

    @Override
    public String resolveTenantId(HttpServletRequest request) {
        if (request == null) {
            return adminProperties.defaultTenantId();
        }
        String tenantId = request.getHeader(adminProperties.tenantHeader());
        if (StringUtils.isBlank(tenantId)) {
            tenantId = request.getParameter("tenantId");
        }
        return adminTenantSupport.requiredTenantId(tenantId);
    }

    @Override
    public SecurityTenant loadTenant(String tenantId) {
        String resolvedTenantId = adminTenantSupport.requiredTenantId(tenantId);
        TenantProfile profile = tenantProfileStore.load(resolvedTenantId);
        if (profile == null) {
            return new SecurityTenant(resolvedTenantId, resolveIssuer(resolvedTenantId), null, true);
        }
        return new SecurityTenant(
                profile.tenantId(),
                resolveIssuer(profile.tenantId()),
                null,
                profile.tenantLineEnabled()
        );
    }

    private String resolveIssuer(String tenantId) {
        String template = adminProperties.tenantIssuerTemplate();
        if (StringUtils.isBlank(template)) {
            return null;
        }
        return template.replace("{tenantId}", StringUtils.defaultString(tenantId));
    }
}
