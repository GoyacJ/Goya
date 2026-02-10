package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * <p>认证结果 Token。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class AuthResultAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthResult authResult;

    public AuthResultAuthenticationToken(AuthResult authResult) {
        super(Collections.emptyList());
        this.authResult = authResult;
        setAuthenticated(true);
    }

    public AuthResult getAuthResult() {
        return authResult;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return authResult == null ? null : authResult.status();
    }
}
