package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.MfaVerifyRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * <p>MFA 校验认证请求 Token。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class MfaVerifyAuthenticationToken extends AbstractAuthenticationToken {

    private final MfaVerifyRequest request;
    private final HttpServletRequest servletRequest;

    public MfaVerifyAuthenticationToken(MfaVerifyRequest request, HttpServletRequest servletRequest) {
        super(Collections.emptyList());
        this.request = request;
        this.servletRequest = servletRequest;
        setAuthenticated(false);
    }

    public MfaVerifyRequest getRequest() {
        return request;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public Object getCredentials() {
        return request == null ? null : request.code();
    }

    @Override
    public Object getPrincipal() {
        return request == null ? null : request.challengeId();
    }
}
