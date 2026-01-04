package com.ysmjjsy.goya.security.authentication.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/2 15:32
 */
@Slf4j
public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {

    protected Set<String> validateScopes(Set<String> requestedScopes, RegisteredClient registeredClient) {
        Set<String> authorizedScopes = Collections.emptySet();
        if (!CollectionUtils.isEmpty(requestedScopes)) {
            for (String requestedScope : requestedScopes) {
                if (!registeredClient.getScopes().contains(requestedScope)) {
                    throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
                }
            }
            authorizedScopes = new LinkedHashSet<>(requestedScopes);
        }
        return authorizedScopes;
    }

    /**
     * 验证客户端认证
     *
     * @param authentication 认证Token
     * @return 客户端认证Token
     * @throws OAuth2AuthenticationException 如果客户端认证失败
     */
    protected OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
            Authentication authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
        }
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
    }

    /**
     * 获取已注册的客户端
     *
     * @param clientPrincipal 客户端认证Token
     * @return 已注册的客户端
     * @throws OAuth2AuthenticationException 如果客户端认证失败
     */
    protected RegisteredClient getRegisteredClient(OAuth2ClientAuthenticationToken clientPrincipal) {
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
        if (registeredClient == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.INVALID_CLIENT));
        }
        return registeredClient;
    }

    /**
     * 验证授权类型
     *
     * @param registeredClient 已注册的客户端
     * @param grantType 授权类型
     * @throws OAuth2AuthenticationException 如果授权类型不支持
     */
    protected void checkAuthorizedGrantType(RegisteredClient registeredClient, AuthorizationGrantType grantType) {
        if (!registeredClient.getAuthorizationGrantTypes().contains(grantType)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT));
        }
    }

    /**
     * 验证并确定授权的scope
     *
     * @param registeredClient 已注册的客户端
     * @param requestedScopes  请求的scope集合
     * @return 授权的scope集合
     */
    protected Set<String> validateScopes(RegisteredClient registeredClient, Set<String> requestedScopes) {
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            return registeredClient.getScopes();
        }

        // 验证请求的scope是否在客户端允许的scope范围内
        Set<String> authorizedScopes = registeredClient.getScopes();
        if (!authorizedScopes.containsAll(requestedScopes)) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_SCOPE,
                            "请求的scope不在客户端允许的范围内",
                            null));
        }

        return requestedScopes;
    }
}
