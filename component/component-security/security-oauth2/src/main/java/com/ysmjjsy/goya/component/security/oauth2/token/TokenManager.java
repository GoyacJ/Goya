package com.ysmjjsy.goya.component.security.oauth2.token;

import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.*;
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
import java.util.Set;

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
public class TokenManager {

    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final OAuth2AuthorizationService authorizationService;
    private final SecurityUserManager securityUserManager;
    private final TokenBlacklistStamp tokenBlacklistStamp;

    /**
     * 生成 token
     *
     * @param registeredClient 客户端信息
     * @param userDetails      用户信息
     * @param grantType        授权类型
     * @param authentication   认证对象
     * @param dPoPProof        dPoPProof
     * @return TokenResponse
     */
    public @NonNull TokenResponse generateToken(
            @NonNull RegisteredClient registeredClient,
            @NonNull SecurityUser userDetails,
            @NonNull AuthorizationGrantType grantType,
            Set<String> authorizedScopes,
            Authentication authentication,
            Jwt dPoPProof) {
        if (authentication == null) {
            authentication = new OAuth2GrantAuthenticationToken(userDetails, grantType, authorizedScopes);
        }

        // 创建principal Authentication对象（principal方法需要Authentication类型）
        Authentication principal = authentication;

        // 1. 生成access_token
        DefaultOAuth2TokenContext.Builder builder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(principal)
                .authorizedScopes(authorizedScopes)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizationGrantType(grantType)
                .authorizationGrant(authentication)
                .put(OAuth2TokenContext.DPOP_PROOF_KEY, dPoPProof);

        // 验证Token Type配置（必须在生成Token之前验证）
        validateTokenTypeConfiguration(registeredClient, dPoPProof);

        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(builder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build());
        if (generatedAccessToken == null) {
            OAuth2Error error = new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "Token生成器无法生成access token",
                    null);
            throw new OAuth2AuthenticationException(error);
        }

        // 根据客户端配置确定TokenType（按客户端配置，符合OAuth2.1规范）
        // 参考：https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/dpop-tokens.html
        OAuth2AccessToken.TokenType tokenType = determineTokenType(registeredClient, dPoPProof);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                tokenType,
                generatedAccessToken.getTokenValue(),
                generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(),
                authorizedScopes);

        // 2. 生成refresh_token
        OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(builder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build());

        // 3. 创建OAuth2Authorization
        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(userDetails.getUsername())
                .authorizationGrantType(grantType)
                .authorizedScopes(authorizedScopes)
                .accessToken(accessToken);

        if (generatedRefreshToken != null) {
            OAuth2RefreshToken refreshToken = new OAuth2RefreshToken(generatedRefreshToken.getTokenValue(),
                    generatedRefreshToken.getIssuedAt(),
                    generatedRefreshToken.getExpiresAt());
            authorizationBuilder.refreshToken(refreshToken);
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

        // 根据TokenType设置正确的token_type（符合Spring Security官方规范）
        // 参考：https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/dpop-tokens.html
        String tokenTypeValue = tokenType.getValue();

        // 记录Token生成审计日志
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String ipAddress = getClientIp(request);
                securityUserManager.recordTokenGenerate(
                        userDetails.getUserId(),
                        userDetails.getUsername(),
                        registeredClient.getClientId(),
                        ipAddress,
                        request.getRequestURI()
                );
            }
        } catch (Exception e) {
            log.warn("[Goya] |- security [authentication] Failed to record token generate audit log", e);
        }

        return TokenResponse.of(
                accessToken.getTokenValue(),
                refreshTokenValue,
                tokenTypeValue,
                expiresIn,
                issuedAt,
                expiresAt
        );
    }

    /**
     * 刷新Token（支持Refresh Token Rotation和DPoP）
     * <p>每次刷新时，旧的Refresh Token自动作废，颁发新的</p>
     * <p>支持DPoP Proof验证（如果客户端配置为DPoP Token Type）</p>
     * <p>此方法会自动从当前HTTP请求中提取DPoP Proof（如果存在）</p>
     *
     * @param refreshTokenValue Refresh Token值
     * @param registeredClient  客户端信息
     * @return Token响应
     */
    public TokenResponse refreshToken(String refreshTokenValue, RegisteredClient registeredClient) {
        // 尝试从HTTP请求中提取DPoP Proof
        Jwt dPoPProof = extractDPoPProofFromRequest();
        return refreshToken(refreshTokenValue, registeredClient, dPoPProof);
    }

    /**
     * 刷新Token（支持Refresh Token Rotation和DPoP）
     * <p>每次刷新时，旧的Refresh Token自动作废，颁发新的</p>
     * <p>支持DPoP Proof验证（如果客户端配置为DPoP Token Type）</p>
     *
     * @param refreshTokenValue Refresh Token值
     * @param registeredClient  客户端信息
     * @param dPoPProof         DPoP Proof（可选，如果客户端配置为DPoP则必需）
     * @return Token响应
     */
    public TokenResponse refreshToken(String refreshTokenValue, RegisteredClient registeredClient, Jwt dPoPProof) {
        // 1. 查找授权记录
        OAuth2Authorization authorization = authorizationService.findByToken(
                refreshTokenValue, OAuth2TokenType.REFRESH_TOKEN);

        if (authorization == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, "Refresh token无效", null));
        }

        // 2. 验证Refresh Token是否已过期
        OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                authorization.getToken(OAuth2RefreshToken.class);
        if (refreshToken == null || refreshToken.isExpired()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.INVALID_GRANT, "Refresh token已过期", null));
        }

        // 3. 删除旧的授权记录（Refresh Token Rotation）
        authorizationService.remove(authorization);

        // 4. 将旧的Refresh Token加入黑名单
        tokenBlacklistStamp.addToBlacklist(
                refreshTokenValue,
                "Refresh token rotated",
                Duration.ofDays(1)); // 保留1天，防止并发刷新

        // 5. 提取用户信息
        SecurityUser user = extractUser(authorization);

        // 6. 生成新的Token（支持DPoP Proof）
        TokenResponse response = generateToken(
                registeredClient,
                user,
                AuthorizationGrantType.REFRESH_TOKEN,
                authorization.getAuthorizedScopes(),
                null,
                dPoPProof);

        // 记录Token刷新审计日志
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request != null) {
                String ipAddress = getClientIp(request);
                securityUserManager.recordTokenRefresh(
                        user.getUserId(),
                        user.getUsername(),
                        registeredClient.getClientId(),
                        ipAddress,
                        request.getRequestURI()
                );
            }
        } catch (Exception e) {
            log.warn("[Goya] |- security [authentication] Failed to record token refresh audit log", e);
        }

        return response;
    }

    /**
     * 从HTTP请求中提取并验证DPoP Proof
     * <p>从请求的DPoP header中提取DPoP Proof并验证</p>
     * <p>根据RFC 9449，DPoP Proof应该在DPoP header中提供</p>
     *
     * @return 验证后的DPoP Proof JWT，如果不存在则返回null
     */
    private Jwt extractDPoPProofFromRequest() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return null;
            }

            String dPoPProofHeader = request.getHeader("DPoP");
            if (dPoPProofHeader == null || dPoPProofHeader.isBlank()) {
                return null;
            }

            // 使用Spring Security的DPoP Proof验证API
            String method = request.getMethod();
            String targetUri = request.getRequestURL().toString();
            if (request.getQueryString() != null) {
                targetUri += "?" + request.getQueryString();
            }

            // 构建DPoP Proof验证上下文
            DPoPProofContext dPoPProofContext = DPoPProofContext.withDPoPProof(dPoPProofHeader)
                    .method(method)
                    .targetUri(targetUri)
                    .build();

            // 创建JWT解码器并验证DPoP Proof
            JwtDecoder dPoPProofVerifier = new DPoPProofJwtDecoderFactory().createDecoder(dPoPProofContext);
            Jwt dPoPProofJwt = dPoPProofVerifier.decode(dPoPProofHeader);
            
            log.debug("[Goya] |- security [oauth2] DPoP Proof extracted and validated from refresh token request");
            return dPoPProofJwt;
        } catch (Exception e) {
            // DPoP Proof验证失败，但不抛出异常（允许不使用DPoP的情况）
            log.debug("[Goya] |- security [oauth2] Failed to extract DPoP Proof from request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从OAuth2Authorization中提取SecurityUser
     * <p>通过principalName（username）查找用户</p>
     *
     * @param authorization 授权记录
     * @return 用户信息
     */
    private SecurityUser extractUser(OAuth2Authorization authorization) {
        String principalName = authorization.getPrincipalName();
        if (principalName == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "授权记录中缺少principalName", null));
        }

        try {
            SecurityUser user = securityUserManager.findUserByUsername(principalName);
            if (user == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "用户不存在: " + principalName, null));
            }
            return user;
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to find user by principalName: {}", principalName, e);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR, "用户查找失败", null), e);
        }
    }

    /**
     * 将Token加入黑名单
     *
     * @param token Token值
     */
    public void addToBlacklist(String token) {
        tokenBlacklistStamp.addToBlacklist(token);
    }

    /**
     * 将Token加入黑名单
     *
     * @param token  Token值
     * @param expire 过期时间
     */
    public void addToBlacklist(String token, Duration expire) {
        tokenBlacklistStamp.addToBlacklist(token, expire);
    }

    /**
     * 将Token加入黑名单
     *
     * @param token  Token值
     * @param reason 原因
     * @param expire 过期时间
     */
    public void addToBlacklist(String token, String reason, Duration expire) {
        tokenBlacklistStamp.addToBlacklist(token, reason, expire);
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token Token值
     * @return true如果在黑名单中，false如果不在
     */
    public boolean isBlacklisted(String token) {
        return tokenBlacklistStamp.isBlacklisted(token);
    }

    /**
     * 从黑名单移除Token（通常不需要，因为Token会自动过期）
     *
     * @param token Token值
     */
    public void removeFromBlacklist(String token) {
        tokenBlacklistStamp.removeFromBlacklist(token);
    }

    /**
     * 验证Token Type配置
     * <p>根据客户端配置验证DPoP Proof是否必需</p>
     *
     * @param registeredClient 注册的客户端
     * @param dPoPProof        DPoP Proof（如果提供）
     * @throws OAuth2AuthenticationException 如果配置为DPoP但没有提供DPoP Proof
     */
    private void validateTokenTypeConfiguration(RegisteredClient registeredClient, Jwt dPoPProof) {
        String tokenType = getClientTokenType(registeredClient);
        if ("DPoP".equalsIgnoreCase(tokenType)) {
            if (dPoPProof == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error(
                                OAuth2ErrorCodes.INVALID_REQUEST,
                                "Client requires DPoP Token Type but DPoP Proof is missing",
                                null
                        )
                );
            }
            log.debug("[Goya] |- security [authentication] Client {} requires DPoP Token Type, DPoP Proof validated", 
                    registeredClient.getClientId());
        }
    }

    /**
     * 确定Token Type
     * <p>根据客户端配置确定使用BEARER还是DPoP</p>
     *
     * @param registeredClient 注册的客户端
     * @param dPoPProof        DPoP Proof（如果提供）
     * @return Token Type
     */
    private OAuth2AccessToken.TokenType determineTokenType(RegisteredClient registeredClient, Jwt dPoPProof) {
        String tokenType = getClientTokenType(registeredClient);
        
        // 如果客户端配置为DPoP，且提供了DPoP Proof，则使用DPoP
        if ("DPoP".equalsIgnoreCase(tokenType) && dPoPProof != null) {
            return OAuth2AccessToken.TokenType.DPOP;
        }
        
        // 否则使用BEARER（包括：未配置、配置为BEARER、或配置为DPoP但没有Proof的情况）
        // 注意：如果配置为DPoP但没有Proof，会在validateTokenTypeConfiguration中抛出异常
        return OAuth2AccessToken.TokenType.BEARER;
    }

    /**
     * 获取当前HTTP请求
     *
     * @return HttpServletRequest，如果不在请求上下文中则返回null
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取客户端的Token Type配置
     * <p>从RegisteredClient的clientSettings中获取token.type配置</p>
     * <p>如果未配置，返回null（将使用默认BEARER）</p>
     *
     * @param registeredClient 注册的客户端
     * @return Token Type配置（"BEARER"、"DPoP"或null）
     */
    private String getClientTokenType(RegisteredClient registeredClient) {
        if (registeredClient == null || registeredClient.getClientSettings() == null) {
            return null;
        }
        
        // 从clientSettings中获取token.type配置
        // 支持两种key名称：token.type 和 token_type
        String tokenType = registeredClient.getClientSettings().getSetting("token.type");
        if (tokenType == null) {
            tokenType = registeredClient.getClientSettings().getSetting("token_type");
        }
        
        return tokenType;
    }
}
