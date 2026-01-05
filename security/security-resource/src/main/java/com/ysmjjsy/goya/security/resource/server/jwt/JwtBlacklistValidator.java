package com.ysmjjsy.goya.security.resource.server.jwt;

import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.security.resource.server.configuration.properties.SecurityResourceProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

/**
 * <p>JWT黑名单验证器</p>
 * <p>验证JWT是否在黑名单中（通过JTI或完整Token值）</p>
 * <p>用于Token撤销场景</p>
 *
 * <p>验证逻辑：</p>
 * <ul>
 *   <li>如果JWT包含jti claim，检查jti是否在黑名单中</li>
 *   <li>如果JWT不包含jti，检查完整Token值是否在黑名单中</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class JwtBlacklistValidator implements JwtDecoder {

    private final JwtDecoder delegate;
    private final ICacheService cacheService;
    private final SecurityResourceProperties.TokenBlacklistConfig tokenBlacklistConfig;

    @Override
    public Jwt decode(String token) throws JwtException {
        // 1. 先使用委托的JwtDecoder解码JWT
        Jwt jwt = delegate.decode(token);

        // 2. 如果黑名单检查未启用，直接返回
        if (!tokenBlacklistConfig.enabled()) {
            return jwt;
        }

        // 3. 检查Token是否在黑名单中
        boolean isBlacklisted = false;

        // 优先检查JTI（JWT ID）
        String jti = jwt.getId();
        if (jti != null) {
            String jtiKey = "token:" + jti;
            isBlacklisted = cacheService.exists(tokenBlacklistConfig.cacheName(), jtiKey);
            if (isBlacklisted) {
                log.warn("[Goya] |- security [resource] JWT with jti {} is blacklisted", jti);
            }
        }

        // 如果JTI不在黑名单中，检查完整Token值（用于没有JTI的Token）
        if (!isBlacklisted) {
            String tokenKey = "token:" + token;
            isBlacklisted = cacheService.exists(tokenBlacklistConfig.cacheName(), tokenKey);
            if (isBlacklisted) {
                log.warn("[Goya] |- security [resource] JWT token is blacklisted");
            }
        }

        // 4. 如果Token在黑名单中，抛出异常
        if (isBlacklisted) {
            throw new JwtException(
                    new OAuth2Error(
                            OAuth2ErrorCodes.INVALID_TOKEN,
                            "Token已被撤销",
                            null
                    ).toString()
            );
        }

        return jwt;
    }
}

