package com.ysmjjsy.goya.security.core.context;

import com.ysmjjsy.goya.component.web.service.AbstractPlatformService;
import com.ysmjjsy.goya.security.core.configuration.SecurityCoreProperties;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/13 16:09
 */
public class SecurityPlatformService extends AbstractPlatformService {

    private final SecurityCoreProperties securityCoreProperties;

    public SecurityPlatformService(ServerProperties serverProperties, SecurityCoreProperties securityCoreProperties) {
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
}
