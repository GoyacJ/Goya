package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.service.SocialAuthService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>社交登录 Provider。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SocialAuthenticationProvider implements AuthenticationProvider {

    private final SocialAuthService socialAuthService;

    public SocialAuthenticationProvider(SocialAuthService socialAuthService) {
        this.socialAuthService = socialAuthService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
        AuthResult authResult = socialAuthService.callback(
                socialAuthenticationToken.getSource(),
                socialAuthenticationToken.getCallbackParams(),
                socialAuthenticationToken.getServletRequest()
        );
        return new AuthResultAuthenticationToken(authResult);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
