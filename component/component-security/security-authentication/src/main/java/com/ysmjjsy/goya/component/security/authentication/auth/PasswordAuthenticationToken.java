package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.PasswordLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * <p>密码登录认证请求 Token。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PasswordAuthenticationToken extends AbstractAuthenticationToken {

    private final PasswordLoginRequest request;
    private final HttpServletRequest servletRequest;

    public PasswordAuthenticationToken(PasswordLoginRequest request, HttpServletRequest servletRequest) {
        super(Collections.emptyList());
        this.request = request;
        this.servletRequest = servletRequest;
        setAuthenticated(false);
    }

    public PasswordLoginRequest getRequest() {
        return request;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public Object getCredentials() {
        return request == null ? null : request.password();
    }

    @Override
    public Object getPrincipal() {
        return request == null ? null : request.username();
    }
}
