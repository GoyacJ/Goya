package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.service.MfaService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * <p>MFA 校验 Provider。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class MfaVerifyAuthenticationProvider implements AuthenticationProvider {

    private final MfaService mfaService;

    public MfaVerifyAuthenticationProvider(MfaService mfaService) {
        this.mfaService = mfaService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        MfaVerifyAuthenticationToken mfaVerifyAuthenticationToken = (MfaVerifyAuthenticationToken) authentication;
        AuthResult authResult = mfaService.verifyAndIssuePreAuth(
                mfaVerifyAuthenticationToken.getRequest(),
                mfaVerifyAuthenticationToken.getServletRequest()
        );
        return new AuthResultAuthenticationToken(authResult);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MfaVerifyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
