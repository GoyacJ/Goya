package com.ysmjjsy.goya.security.authentication.provider.password;

import com.ysmjjsy.goya.security.authentication.enums.LoginGrantType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;
import org.springframework.util.Assert;

import java.io.Serial;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>密码Grant Type认证Token</p>
 * <p>包含用户名、密码、验证码等凭证信息，用于Password Grant Type的认证流程</p>
 *
 * <p>继承自OAuth2AuthorizationGrantAuthenticationToken，符合Spring Authorization Server的认证Token规范</p>
 *
 * @author goya
 * @see PasswordGrantAuthenticationConverter
 * @see PasswordGrantAuthenticationProvider
 * @since 2025/12/21
 */
@EqualsAndHashCode(callSuper = true)
@Getter
public class PasswordGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    @Serial
    private static final long serialVersionUID = -2773565451322267601L;

    /**
     * 请求的 scope 集合
     */
    private final Set<String> scopes;


    /**
     * 构造函数
     *
     * @param clientPrincipal      客户端认证信息
     * @param scopes               请求的scope集合
     * @param additionalParameters 额外的参数
     */
    public PasswordGrantAuthenticationToken(
            Authentication clientPrincipal, Set<String> scopes, Map<String, Object> additionalParameters) {
        super(LoginGrantType.PASSWORD.getGrantType(), clientPrincipal, additionalParameters);
        Assert.notNull(clientPrincipal, "clientPrincipal cannot be null");
        this.scopes = Collections.unmodifiableSet(CollectionUtils.isNotEmpty(scopes) ? new HashSet<>(scopes) : Collections.emptySet());
    }
}

