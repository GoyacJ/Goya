package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.service.SmsAuthService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>短信登录 Provider。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private final SmsAuthService smsAuthService;

    public SmsAuthenticationProvider(SmsAuthService smsAuthService) {
        this.smsAuthService = smsAuthService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken smsAuthenticationToken = (SmsAuthenticationToken) authentication;
        AuthResult authResult = smsAuthService.login(
                smsAuthenticationToken.getRequest(),
                smsAuthenticationToken.getServletRequest()
        );
        return new AuthResultAuthenticationToken(authResult);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
