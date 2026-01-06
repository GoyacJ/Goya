package com.ysmjjsy.goya.security.authentication.provider.login;

import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.Serial;
import java.util.Map;

/**
 * <p>短信登录认证Token</p>
 * <p>用于短信验证码登录方式的认证</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@EqualsAndHashCode(callSuper = true)
public class SmsAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = 5987700557462563086L;

    private final String phoneNumber;
    private String smsCode;
    private final Map<String, Object> additionalParameters;

    public SmsAuthenticationToken(
            String phoneNumber,
            String smsCode,
            Map<String, Object> additionalParameters) {

        super(AuthorityUtils.NO_AUTHORITIES);
        this.phoneNumber = phoneNumber;
        this.smsCode = smsCode;
        this.additionalParameters =
                additionalParameters != null ? additionalParameters : Map.of();

        super.setAuthenticated(false);
    }

    @Override
    public Object getPrincipal() {
        return this.phoneNumber;
    }

    @Override
    public Object getCredentials() {
        return this.smsCode;
    }

    @Override
    public Object getDetails() {
        return this.additionalParameters;
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        this.smsCode = null;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "SmsAuthenticationToken cannot be marked as authenticated"
            );
        }
        super.setAuthenticated(false);
    }
}

