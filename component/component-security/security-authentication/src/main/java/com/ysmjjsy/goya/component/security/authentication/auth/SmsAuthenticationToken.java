package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.SmsLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * <p>短信登录认证请求 Token。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SmsAuthenticationToken extends AbstractAuthenticationToken {

    private final SmsLoginRequest request;
    private final HttpServletRequest servletRequest;

    public SmsAuthenticationToken(SmsLoginRequest request, HttpServletRequest servletRequest) {
        super(Collections.emptyList());
        this.request = request;
        this.servletRequest = servletRequest;
        setAuthenticated(false);
    }

    public SmsLoginRequest getRequest() {
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
        return request == null ? null : request.phoneNumber();
    }
}
