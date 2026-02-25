package com.ysmjjsy.goya.component.security.oauth2.grant;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>预认证码授权类型 Token</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PreAuthCodeGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    private final String preAuthCode;
    private final Set<String> scopes;

    public PreAuthCodeGrantAuthenticationToken(AuthorizationGrantType authorizationGrantType,
                                               Authentication clientPrincipal,
                                               String preAuthCode,
                                               Set<String> scopes,
                                               Map<String, Object> additionalParameters) {
        super(authorizationGrantType, clientPrincipal, additionalParameters);
        this.preAuthCode = preAuthCode;
        this.scopes = scopes == null ? Collections.emptySet() : Collections.unmodifiableSet(new LinkedHashSet<>(scopes));
    }

    public String getPreAuthCode() {
        return preAuthCode;
    }

    public Set<String> getScopes() {
        return scopes;
    }
}
