package com.ysmjjsy.goya.component.security.oauth2.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

/**
 * <p>AuthorizationServerContext 默认实现</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@RequiredArgsConstructor
public class DefaultAuthorizationServerContext implements AuthorizationServerContext {

    private final AuthorizationServerSettings authorizationServerSettings;
    private final String issuer;

    @Override
    public String getIssuer() {
        return issuer;
    }

    @Override
    public AuthorizationServerSettings getAuthorizationServerSettings() {
        return authorizationServerSettings;
    }
}
