package com.ysmjjsy.goya.security.authentication.provider.login;

import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.Serial;
import java.util.Map;

/**
 * <p>密码登录认证Token</p>
 * <p>用于密码登录方式的认证</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@EqualsAndHashCode(callSuper = true)
public final class PasswordAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private String password;
    private final Map<String, Object> additionalParameters;

    public PasswordAuthenticationToken(
            String username,
            String password,
            Map<String, Object> additionalParameters) {

        super(AuthorityUtils.NO_AUTHORITIES);
        this.username = username;
        this.password = password;
        this.additionalParameters =
                additionalParameters != null ? additionalParameters : Map.of();

        super.setAuthenticated(false);
    }

    @Override
    public Object getPrincipal() {
        return this.username;
    }

    @Override
    public Object getCredentials() {
        return this.password;
    }

    @Override
    public Object getDetails() {
        return this.additionalParameters;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.password = null;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "PasswordAuthenticationToken cannot be set to authenticated directly"
            );
        }
        super.setAuthenticated(false);
    }
}
