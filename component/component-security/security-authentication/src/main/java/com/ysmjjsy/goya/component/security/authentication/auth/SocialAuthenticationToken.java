package com.ysmjjsy.goya.component.security.authentication.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;
import java.util.Map;

/**
 * <p>社交登录认证请求 Token。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SocialAuthenticationToken extends AbstractAuthenticationToken {

    private final String source;
    private final Map<String, String> callbackParams;
    private final HttpServletRequest servletRequest;

    public SocialAuthenticationToken(String source,
                                     Map<String, String> callbackParams,
                                     HttpServletRequest servletRequest) {
        super(Collections.emptyList());
        this.source = source;
        this.callbackParams = callbackParams;
        this.servletRequest = servletRequest;
        setAuthenticated(false);
    }

    public String getSource() {
        return source;
    }

    public Map<String, String> getCallbackParams() {
        return callbackParams;
    }

    public HttpServletRequest getServletRequest() {
        return servletRequest;
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return source;
    }
}
