package com.ysmjjsy.goya.security.authentication.provider.authentication;

import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Map;

/**
 * <p>密码登录认证Token</p>
 * <p>用于密码登录方式的认证</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
public class PasswordAuthenticationToken implements Authentication {

    private final String username;
    private final String password;
    private final String captcha;
    private final String captchaKey;
    private final Map<String, Object> additionalParams;
    private boolean authenticated = false;

    public PasswordAuthenticationToken(
            String username,
            String password,
            String captcha,
            String captchaKey,
            Map<String, Object> additionalParams) {
        this.username = username;
        this.password = password;
        this.captcha = captcha;
        this.captchaKey = captchaKey;
        this.additionalParams = additionalParams != null ? additionalParams : Collections.emptyMap();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public String getCaptchaKey() {
        return captchaKey;
    }

    public Map<String, Object> getAdditionalParams() {
        return additionalParams;
    }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getDetails() {
        return additionalParams;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return username;
    }
}

