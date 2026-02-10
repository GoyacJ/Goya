package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.service.WxMiniProgramAuthService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>小程序登录 Provider。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class WxMiniProgramAuthenticationProvider implements AuthenticationProvider {

    private final WxMiniProgramAuthService wxMiniProgramAuthService;

    public WxMiniProgramAuthenticationProvider(WxMiniProgramAuthService wxMiniProgramAuthService) {
        this.wxMiniProgramAuthService = wxMiniProgramAuthService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        WxMiniProgramAuthenticationToken wxMiniProgramAuthenticationToken = (WxMiniProgramAuthenticationToken) authentication;
        AuthResult authResult = wxMiniProgramAuthService.login(
                wxMiniProgramAuthenticationToken.getRequest(),
                wxMiniProgramAuthenticationToken.getServletRequest()
        );
        return new AuthResultAuthenticationToken(authResult);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return WxMiniProgramAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
