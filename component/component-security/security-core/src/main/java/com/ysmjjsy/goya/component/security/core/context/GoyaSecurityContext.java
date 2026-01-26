package com.ysmjjsy.goya.component.security.core.context;

import com.ysmjjsy.goya.component.framework.context.GoyaUser;
import com.ysmjjsy.goya.component.framework.tenant.TenantContext;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.web.context.AbstractGoyaContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

/**
 * <p>认证服务</p>
 *
 * @author goya
 * @since 2025/12/31 11:47
 */
public class GoyaSecurityContext extends AbstractGoyaContext {

    private final SecurityCoreProperties securityCoreProperties;

    public GoyaSecurityContext(ServerProperties serverProperties, SecurityCoreProperties securityCoreProperties) {
        super(serverProperties);
        this.securityCoreProperties = securityCoreProperties;
    }

    @Override
    public String getAuthServiceUri() {
        return securityCoreProperties.authServiceUri();
    }

    @Override
    public String getAuthServiceName() {
        return securityCoreProperties.authServiceName();
    }

    @Override
    public GoyaUser currentUser() {
        return super.currentUser();
    }

    @Override
    public GoyaUser currentUser(HttpServletRequest request) {
        return super.currentUser(request);
    }

    @Override
    public GoyaUser currentUser(String token) {
        return super.currentUser(token);
    }

    @Override
    public String currentTenant() {
        return TenantContext.getTenantId();
    }
}
