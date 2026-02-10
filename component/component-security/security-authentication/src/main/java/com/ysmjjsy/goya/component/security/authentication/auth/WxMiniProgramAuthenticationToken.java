package com.ysmjjsy.goya.component.security.authentication.auth;

import com.ysmjjsy.goya.component.security.authentication.dto.WxMiniLoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * <p>微信小程序登录认证请求 Token。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class WxMiniProgramAuthenticationToken extends AbstractAuthenticationToken {

    private final WxMiniLoginRequest request;
    private final HttpServletRequest servletRequest;

    public WxMiniProgramAuthenticationToken(WxMiniLoginRequest request, HttpServletRequest servletRequest) {
        super(Collections.emptyList());
        this.request = request;
        this.servletRequest = servletRequest;
        setAuthenticated(false);
    }

    public WxMiniLoginRequest getRequest() {
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
        return request == null ? null : request.appId();
    }
}
