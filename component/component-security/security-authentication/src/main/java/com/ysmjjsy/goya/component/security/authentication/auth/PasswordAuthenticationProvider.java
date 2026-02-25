package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.service.PasswordAuthService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>密码登录 Provider。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PasswordAuthenticationProvider implements AuthenticationProvider {

    private final PasswordAuthService passwordAuthService;

    public PasswordAuthenticationProvider(PasswordAuthService passwordAuthService) {
        this.passwordAuthService = passwordAuthService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PasswordAuthenticationToken passwordAuthenticationToken = (PasswordAuthenticationToken) authentication;
        AuthResult authResult = passwordAuthService.login(
                passwordAuthenticationToken.getRequest(),
                passwordAuthenticationToken.getServletRequest()
        );
        return new AuthResultAuthenticationToken(authResult);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
