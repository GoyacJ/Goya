package com.ysmjjsy.goya.component.security.oauth2.service;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import com.ysmjjsy.goya.component.security.oauth2.constants.SecurityOAuth2CacheNames;
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
import java.util.Set;

/**
 * <p>带撤销索引与 sid 索引写入能力的 AuthorizationService 装饰器。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class RevocationIndexingAuthorizationService implements OAuth2AuthorizationService {

    private static final String KEY_SEPARATOR = ":";

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
        OAuth2Authorization.Token<?> refreshToken = authorization.getRefreshToken();
        if ((accessToken == null || accessToken.getToken() == null)
                && (refreshToken == null || refreshToken.getToken() == null)) {
            return;
        }

        Duration ttl = resolveAuthorizationTtl(authorization, securityOAuth2Properties.refreshTokenTtl());

        boolean revoked = forceRevoke || Boolean.TRUE.equals(
                accessToken == null ? null : accessToken.getMetadata().get(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME)
        ) || Boolean.TRUE.equals(
                refreshToken == null ? null : refreshToken.getMetadata().get(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME)
        );
        if (revoked) {
            revokeToken(accessToken, ttl);
            revokeToken(refreshToken, ttl);
            return;
        }

        Map<String, Object> claims = extractClaims(accessToken);
        String sid = toStringValue(claims.get(StandardClaimNamesConst.SID));
        String tenantId = toStringValue(claims.get(StandardClaimNamesConst.TENANT_ID));
        String userId = StringUtils.defaultIfBlank(toStringValue(claims.get("sub")), authorization.getPrincipalName());
        String clientId = StringUtils.defaultIfBlank(
                toStringValue(claims.get(StandardClaimNamesConst.CLIENT_ID)),
                authorization.getRegisteredClientId()
        );

        if (StringUtils.isNotBlank(authorization.getId())) {
            indexAuthorizationIdBySid(sid, authorization.getId(), ttl);
            indexAuthorizationIdByUser(tenantId, userId, authorization.getId(), ttl);
            indexAuthorizationIdByUserClient(tenantId, userId, clientId, authorization.getId(), ttl);
        }

        indexTokenIdBySid(sid, accessToken, ttl);
        indexTokenIdBySid(sid, refreshToken, ttl);
    }

    private void indexAuthorizationIdBySid(String sid, String authorizationId, Duration ttl) {
        if (StringUtils.isBlank(sid) || StringUtils.isBlank(authorizationId)) {
            return;
        }
        indexValueSet(SecurityOAuth2CacheNames.SSO_SID_AUTHORIZATION_INDEX, sid, authorizationId, ttl);
    }

    private void indexAuthorizationIdByUser(String tenantId, String userId, String authorizationId, Duration ttl) {
        if (StringUtils.isAnyBlank(tenantId, userId, authorizationId)) {
            return;
        }
        String userKey = composeUserKey(tenantId, userId);
        indexValueSet(SecurityOAuth2CacheNames.SSO_USER_AUTHORIZATION_INDEX, userKey, authorizationId, ttl);
    }

    private void indexAuthorizationIdByUserClient(String tenantId,
                                                  String userId,
                                                  String clientId,
                                                  String authorizationId,
                                                  Duration ttl) {
        if (StringUtils.isAnyBlank(tenantId, userId, clientId, authorizationId)) {
            return;
        }
        String userClientKey = composeUserClientKey(tenantId, userId, clientId);
        indexValueSet(SecurityOAuth2CacheNames.SSO_USER_CLIENT_AUTHORIZATION_INDEX, userClientKey, authorizationId, ttl);
    }

    private void indexTokenIdBySid(String sid, OAuth2Authorization.Token<?> tokenHolder, Duration ttl) {
        if (StringUtils.isBlank(sid) || tokenHolder == null || tokenHolder.getToken() == null) {
            return;
        }
        Set<String> tokenIds = collectTokenIdentifiers(tokenHolder);
        for (String tokenId : tokenIds) {
            indexValueSet(SecurityOAuth2CacheNames.SSO_SID_TOKEN_INDEX, sid, tokenId, ttl);
        }
    }

    private void revokeToken(OAuth2Authorization.Token<?> tokenHolder, Duration ttl) {
        if (tokenHolder == null || tokenHolder.getToken() == null) {
            return;
        }
        Set<String> tokenIdentifiers = collectTokenIdentifiers(tokenHolder);
        for (String tokenIdentifier : tokenIdentifiers) {
            cacheService.put(securityOAuth2Properties.revocationCacheName(), tokenIdentifier, "1", ttl);
        }
    }

    private Set<String> collectTokenIdentifiers(OAuth2Authorization.Token<?> tokenHolder) {
        LinkedHashSet<String> identifiers = new LinkedHashSet<>();
        Map<String, Object> claims = extractClaims(tokenHolder);
        String jti = toStringValue(claims.get("jti"));
        String tokenValue = resolveTokenValue(tokenHolder.getToken());
        if (StringUtils.isNotBlank(jti)) {
            identifiers.add(jti);
        }
        if (StringUtils.isNotBlank(tokenValue)) {
            identifiers.add(tokenValue);
        }
        return identifiers;
    }

    private void indexValueSet(String cacheName, String key, String value, Duration ttl) {
        if (StringUtils.isAnyBlank(cacheName, key, value)) {
            return;
        }
        String existing = cacheService.get(cacheName, key, String.class);
        LinkedHashSet<String> values = parseValues(existing);
        values.add(value);
        cacheService.put(cacheName, key, String.join(",", values), ttl);
    }

    private Map<String, Object> extractClaims(OAuth2Authorization.Token<?> tokenHolder) {
        if (tokenHolder == null) {
            return Map.of();
        }
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

    private Duration resolveAuthorizationTtl(OAuth2Authorization authorization, Duration fallback) {
        Duration accessTtl = resolveTtl(authorization.getAccessToken() == null ? null : authorization.getAccessToken().getToken(), null);
        Duration refreshTtl = resolveTtl(authorization.getRefreshToken() == null ? null : authorization.getRefreshToken().getToken(), null);
        Duration ttl = maxDuration(accessTtl, refreshTtl);
        if (ttl != null) {
            return ttl;
        }
        if (fallback != null && !fallback.isNegative() && !fallback.isZero()) {
            return fallback;
        }
        return Duration.ofHours(1);
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
        if (fallback == null) {
            return null;
        }
        return Duration.ofHours(1);
    }

    private Duration maxDuration(Duration left, Duration right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return left.compareTo(right) >= 0 ? left : right;
    }

    private LinkedHashSet<String> parseValues(String existing) {
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

    private String composeUserKey(String tenantId, String userId) {
        return tenantId + KEY_SEPARATOR + userId;
    }

    private String composeUserClientKey(String tenantId, String userId, String clientId) {
        return tenantId + KEY_SEPARATOR + userId + KEY_SEPARATOR + clientId;
    }
}
