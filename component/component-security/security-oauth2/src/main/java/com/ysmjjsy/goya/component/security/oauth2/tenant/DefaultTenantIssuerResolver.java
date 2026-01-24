package com.ysmjjsy.goya.component.security.oauth2.tenant;

import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.domain.SecurityTenant;
import com.ysmjjsy.goya.component.security.core.service.ITenantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * <p>默认租户Issuer解析器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@RequiredArgsConstructor
public class DefaultTenantIssuerResolver implements TenantIssuerResolver {

    private final SecurityCoreProperties coreProperties;
    private final ObjectProvider<ITenantService> tenantServiceProvider;

    @Override
    public String resolveIssuer(HttpServletRequest request, String tenantId) {
        ITenantService tenantService = tenantServiceProvider.getIfAvailable();
        if (tenantService != null) {
            SecurityTenant tenant = tenantService.loadTenant(tenantId);
            if (tenant != null && StringUtils.isNotBlank(tenant.issuer())) {
                return tenant.issuer();
            }
        }

        String baseUri = coreProperties.authServiceUri();
        if (StringUtils.isBlank(baseUri)) {
            String requestUrl = request.getRequestURL().toString();
            String requestUri = request.getRequestURI();
            baseUri = requestUrl.substring(0, requestUrl.length() - requestUri.length());
        }
        return baseUri + "/t/" + tenantId;
    }
}
