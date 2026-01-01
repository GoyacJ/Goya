package com.ysmjjsy.goya.security.authentication.token;

import com.ysmjjsy.goya.security.authentication.provider.oauth2.OAuth2GrantAuthenticationToken;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.time.Duration;
import java.time.Instant;

/**
 * <p>Token服务实现</p>
 * 封装OAuth2 Token生成的完整流程：
 * 1. 生成access_token和refresh_token
 * 2. 创建并保存OAuth2Authorization
 * 3. 构建Token响应
 *
 * @author goya
 * @since 2025/12/17 23:54
 */
@Slf4j
@RequiredArgsConstructor
public class TokenService {

    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final OAuth2AuthorizationService authorizationService;
    private final TokenBlacklistStamp tokenBlacklistStamp;

    /**
     * 生成token
     *
     * @param registeredClient 客户端信息
     * @param userDetails      用户信息
     * @param grantType        授权类型
     * @param authentication   认证对象
     * @return
     */
    public @NonNull TokenResponse generateToken(
            @NonNull RegisteredClient registeredClient,
            @NonNull SecurityUser userDetails,
            @NonNull AuthorizationGrantType grantType,
            Authentication authentication) {
        if (authentication == null) {
            authentication = new OAuth2GrantAuthenticationToken(userDetails, grantType);
        }

        // 创建principal Authentication对象（principal方法需要Authentication类型）
        Authentication principal = authentication;

        // 1. 生成access_token
        OAuth2TokenContext accessTokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(principal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(grantType)
                .authorizationGrant(authentication)
                .build();

        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(accessTokenContext);
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Token生成器无法生成access token",
                    null);
            throw new OAuth2AuthenticationException(error);
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(),
                generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(),
                null);

        // 2. 生成refresh_token
        OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(principal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                .authorizationGrantType(grantType)
                .authorizationGrant(authentication)
                .build();

        OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(refreshTokenContext);

        // 3. 创建OAuth2Authorization
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userDetails.getUsername())
                .authorizationGrantType(grantType)
                .accessToken(accessToken);

        if (generatedRefreshToken != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(generatedRefreshToken.getTokenValue(),
                    generatedRefreshToken.getIssuedAt(),
                    generatedRefreshToken.getExpiresAt());
            authorizationBuilder.refreshToken(
                    refreshToken
            );
        }

        OAuth2Authorization authorization = authorizationBuilder.build();

        // 4. 保存OAuth2Authorization
        this.authorizationService.save(authorization);

        // 5. 计算过期时间（秒）
        Instant expiresAt = accessToken.getExpiresAt();
        Instant issuedAt = accessToken.getIssuedAt();
        Long expiresIn = null;
        if (expiresAt != null && issuedAt != null) {
            expiresIn = Duration.between(issuedAt, expiresAt).getSeconds();
        }

        // 6. 构建并返回Token响应
        String refreshTokenValue = generatedRefreshToken != null
                ? generatedRefreshToken.getTokenValue()
                : null;

        return TokenResponse.of(
                accessToken.getTokenValue(),
                refreshTokenValue,
                expiresIn,
                issuedAt,
                expiresAt
        );
    }

    /**
     * 将Token加入黑名单
     *
     * @param token Token值
     */
    public void addToBlacklist(String token) {
        tokenBlacklistStamp.put(token);
    }

    /**
     * 将Token加入黑名单
     *
     * @param token  Token值
     * @param expire 过期时间，如果为null则使用默认值
     */
    public void addToBlacklist(String token, Duration expire) {
        tokenBlacklistStamp.put(token, expire);
    }

    /**
     * 将Token加入黑名单
     *
     * @param token  Token值
     * @param reason 原因
     * @param expire 过期时间，如果为null则使用默认值
     */
    public void addToBlacklist(String token, String reason, Duration expire) {
        tokenBlacklistStamp.put(token, reason, expire);
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token Token值
     * @return true如果在黑名单中，false如果不在
     */
    public boolean isBlacklisted(String token) {
        return tokenBlacklistStamp.exists(token);
    }

    /**
     * 从黑名单移除Token（通常不需要，因为Token会自动过期）
     *
     * @param token Token值
     */
    public void removeFromBlacklist(String token) {
        tokenBlacklistStamp.evict(token);
    }
}
