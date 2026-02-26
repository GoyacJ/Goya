package com.ysmjjsy.goya.component.admin.support;

import com.ysmjjsy.goya.component.admin.configuration.properties.AdminProperties;
import com.ysmjjsy.goya.component.security.core.service.SecuritySessionLifecycleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * <p>会话撤销支持</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public class SessionRevokeSupport {

    private final ObjectProvider<SecuritySessionLifecycleService> securitySessionLifecycleServiceProvider;
    private final AdminProperties adminProperties;

    public SessionRevokeSupport(ObjectProvider<SecuritySessionLifecycleService> securitySessionLifecycleServiceProvider,
                                AdminProperties adminProperties) {
        this.securitySessionLifecycleServiceProvider = securitySessionLifecycleServiceProvider;
        this.adminProperties = adminProperties;
    }

    public void revokeByUserIfEnabled(String tenantId, String userId) {
        if (!adminProperties.session().revokeOnRoleChange()) {
            return;
        }
        if (StringUtils.isAnyBlank(tenantId, userId)) {
            return;
        }
        SecuritySessionLifecycleService lifecycleService = securitySessionLifecycleServiceProvider.getIfAvailable();
        if (lifecycleService == null) {
            return;
        }
        lifecycleService.revokeByUser(tenantId, userId);
    }
}
