package com.ysmjjsy.goya.security.authentication.provider.oauth2;

import com.ysmjjsy.goya.component.common.utils.ObjectUtils;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.io.Serial;
import java.util.Collection;

/**
 * <p>OAuth2 Grant Type认证Token</p>
 * 用于OAuth2第三方登录场景的认证Token
 * 包装SecurityUser作为Authentication对象
 *
 * @author goya
 * @since 2025/12/18 09:21
 */
@RequiredArgsConstructor
public class OAuth2GrantAuthenticationToken implements Authentication {

    @Serial
    private static final long serialVersionUID = -4759857447828189616L;

    private final SecurityUser securityUser;
    private final AuthorizationGrantType authorizationGrantType;
    private boolean authenticated = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return ObjectUtils.isNotEmpty(securityUser) ? securityUser.getAuthorities() : AuthorityUtils.NO_AUTHORITIES;
    }

    @Override
    public Object getCredentials() {
        // OAuth2登录不需要凭证
        return null;
    }

    @Override
    public Object getDetails() {
        return getPrincipal();
    }

    @Override
    public Object getPrincipal() {
        return securityUser;
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
        return ObjectUtils.isNotEmpty(securityUser) ? securityUser.getUsername() : "";
    }

    /**
     * 获取授权类型
     *
     * @return 授权类型
     */
    public AuthorizationGrantType getGrantType() {
        return authorizationGrantType;
    }
}
