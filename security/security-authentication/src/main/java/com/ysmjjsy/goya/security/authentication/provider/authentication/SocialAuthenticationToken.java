package com.ysmjjsy.goya.security.authentication.provider.authentication;

import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Map;

/**
 * <p>社交登录认证Token</p>
 * <p>用于第三方社交登录方式的认证</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
public class SocialAuthenticationToken implements Authentication {

    private final String socialProviderId;
    private final Map<String, Object> socialUserAttributes;
    private final Map<String, Object> additionalParams;
    private boolean authenticated = false;

    public SocialAuthenticationToken(
            String socialProviderId,
            Map<String, Object> socialUserAttributes,
            Map<String, Object> additionalParams) {
        this.socialProviderId = socialProviderId;
        this.socialUserAttributes = socialUserAttributes != null ? socialUserAttributes : Collections.emptyMap();
        this.additionalParams = additionalParams != null ? additionalParams : Collections.emptyMap();
    }

    public String getSocialProviderId() {
        return socialProviderId;
    }

    public Map<String, Object> getSocialUserAttributes() {
        return socialUserAttributes;
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
        return null; // 社交登录不需要凭证
    }

    @Override
    public Object getDetails() {
        return additionalParams;
    }

    @Override
    public Object getPrincipal() {
        return socialProviderId;
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
        return socialProviderId;
    }
}

