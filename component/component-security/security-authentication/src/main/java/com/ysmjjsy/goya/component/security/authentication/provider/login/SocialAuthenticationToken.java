package com.ysmjjsy.goya.component.security.authentication.provider.login;

import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import lombok.EqualsAndHashCode;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

import java.io.Serial;
import java.util.Map;

/**
 * <p>社交登录认证Token</p>
 * <p>用于第三方社交登录方式的认证</p>
 * <p>符合Spring Security标准模式</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@EqualsAndHashCode(callSuper = true)
public class SocialAuthenticationToken extends AbstractAuthenticationToken {

    @Serial
    private static final long serialVersionUID = -7230255892235869520L;

    private final SocialTypeEnum socialType;
    private final String source;
    private final Map<String, Object> additionalParameters;

    public SocialAuthenticationToken(
            SocialTypeEnum socialType, String source, Map<String, Object> additionalParameters) {

        super(AuthorityUtils.NO_AUTHORITIES);
        this.socialType = socialType;
        this.source = source;
        this.additionalParameters =
                additionalParameters != null ? additionalParameters : Map.of();

        super.setAuthenticated(false);
    }

    @Override
    public Object getPrincipal() {
        return this.socialType;
    }

    @Override
    public Object getCredentials() {
        return this.source;
    }

    @Override
    public Object getDetails() {
        return this.additionalParameters;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "SocialAuthenticationToken cannot be marked as authenticated"
            );
        }
        super.setAuthenticated(false);
    }
}

