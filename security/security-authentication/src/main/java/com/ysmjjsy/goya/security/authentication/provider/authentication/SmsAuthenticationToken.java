package com.ysmjjsy.goya.security.authentication.provider.authentication;

import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Map;

/**
 * <p>短信登录认证Token</p>
 * <p>用于短信验证码登录方式的认证</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
public class SmsAuthenticationToken implements Authentication {

    private final String phone;
    private final String smsCode;
    private final Map<String, Object> additionalParams;
    private boolean authenticated = false;

    public SmsAuthenticationToken(
            String phone,
            String smsCode,
            Map<String, Object> additionalParams) {
        this.phone = phone;
        this.smsCode = smsCode;
        this.additionalParams = additionalParams != null ? additionalParams : Collections.emptyMap();
    }

    public String getPhone() {
        return phone;
    }

    public String getSmsCode() {
        return smsCode;
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
        return smsCode;
    }

    @Override
    public Object getDetails() {
        return additionalParams;
    }

    @Override
    public Object getPrincipal() {
        return phone;
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
        return phone;
    }
}

