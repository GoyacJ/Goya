package com.ysmjjsy.goya.component.security.oauth2.service;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * <p>带撤销索引与 sid 索引写入能力的 AuthorizationService 装饰器。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class RevocationIndexingAuthorizationService implements OAuth2AuthorizationService {

    private static final String SID_INDEX_CACHE_NAME = "goya:security:sso:sid";

    private final OAuth2AuthorizationService delegate;
    private final CacheService cacheService;
    private final SecurityOAuth2Properties securityOAuth2Properties;

    public RevocationIndexingAuthorizationService(OAuth2AuthorizationService delegate,
                                                  CacheService cacheService,
                                                  SecurityOAuth2Properties securityOAuth2Properties) {
        this.delegate = delegate;
        this.cacheService = cacheService;
        this.securityOAuth2Properties = securityOAuth2Properties;
    }

    @Override
    public void save(OAuth2Authorization authorization) {
        delegate.save(authorization);
        indexAuthorization(authorization, false);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        delegate.remove(authorization);
        indexAuthorization(authorization, true);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return delegate.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return delegate.findByToken(token, tokenType);
    }

    private void indexAuthorization(OAuth2Authorization authorization, boolean forceRevoke) {
        if (authorization == null) {
            return;
        }
        OAuth2Authorization.Token<?> accessToken = authorization.getAccessToken();
        if (accessToken == null || accessToken.getToken() == null) {
            return;
        }

        Map<String, Object> claims = extractClaims(accessToken);
        String jti = toStringValue(claims.get("jti"));
        String sid = toStringValue(claims.get(StandardClaimNamesConst.SID));
        String tokenValue = resolveTokenValue(accessToken.getToken());
        String tokenId = StringUtils.defaultIfBlank(jti, tokenValue);
        Duration ttl = resolveTtl(accessToken.getToken(), securityOAuth2Properties.refreshTokenTtl());

        boolean revoked = forceRevoke || Boolean.TRUE.equals(
                accessToken.getMetadata().get(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME)
        );
        if (revoked) {
            if (StringUtils.isNotBlank(tokenId)) {
                cacheService.put(securityOAuth2Properties.revocationCacheName(), tokenId, "1", ttl);
            }
            return;
        }

        if (StringUtils.isNotBlank(sid) && StringUtils.isNotBlank(tokenId)) {
            String existing = cacheService.get(SID_INDEX_CACHE_NAME, sid, String.class);
            LinkedHashSet<String> tokenIds = parseTokenIds(existing);
            tokenIds.add(tokenId);
            cacheService.put(SID_INDEX_CACHE_NAME, sid, String.join(",", tokenIds), ttl);
        }
    }

    private Map<String, Object> extractClaims(OAuth2Authorization.Token<?> tokenHolder) {
        Object metadataClaims = tokenHolder.getMetadata().get(OAuth2Authorization.Token.CLAIMS_METADATA_NAME);
        if (metadataClaims instanceof Map<?, ?> map) {
            Map<String, Object> claims = new LinkedHashMap<>();
            map.forEach((key, value) -> claims.put(String.valueOf(key), value));
            return claims;
        }

        OAuth2Token token = tokenHolder.getToken();
        if (token instanceof ClaimAccessor claimAccessor) {
            return claimAccessor.getClaims();
        }
        return Map.of();
    }

    private Duration resolveTtl(Object token, Duration fallback) {
        if (token instanceof OAuth2Token oauth2Token && oauth2Token.getExpiresAt() != null) {
            Duration duration = Duration.between(Instant.now(), oauth2Token.getExpiresAt()).plusMinutes(1);
            if (!duration.isNegative() && !duration.isZero()) {
                return duration;
            }
        }
        if (fallback != null && !fallback.isNegative() && !fallback.isZero()) {
            return fallback;
        }
        return Duration.ofHours(1);
    }

    private LinkedHashSet<String> parseTokenIds(String existing) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (StringUtils.isBlank(existing)) {
            return values;
        }
        String[] parts = existing.split(",");
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                values.add(part.trim());
            }
        }
        return values;
    }

    private String toStringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return StringUtils.isBlank(text) ? null : text;
    }

    private String resolveTokenValue(Object token) {
        if (token instanceof OAuth2Token oauth2Token) {
            return toStringValue(oauth2Token.getTokenValue());
        }
        return null;
    }
}
