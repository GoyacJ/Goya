package com.ysmjjsy.goya.security.core.context;

import com.ysmjjsy.goya.component.common.service.IPropertiesCacheService;
import com.ysmjjsy.goya.component.web.context.AbstractPlatformHolder;
import org.springframework.boot.autoconfigure.web.ServerProperties;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/13 16:09
 */
public class SecurityServiceContextHolder extends AbstractPlatformHolder {

    private final SecurityCoreProperties endpointProperties;

    public SecurityServiceContextHolder(IPropertiesCacheService iPropertiesCacheService,
                                        ServerProperties serverProperties,
                                        SecurityCoreProperties endpointProperties) {
        super(iPropertiesCacheService, serverProperties);
        this.endpointProperties = endpointProperties;
    }

    @Override
    public String getAuthServiceUri() {
        return endpointProperties.getAuthServiceUri();
    }

    @Override
    public String getAuthServiceName() {
        return endpointProperties.authServiceName();
    }
}
