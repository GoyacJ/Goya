package com.ysmjjsy.goya.component.security.oauth2.service;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.security.core.domain.SecuritySessionRevocationResult;
import com.ysmjjsy.goya.component.security.core.service.SecuritySessionLifecycleService;
import com.ysmjjsy.goya.component.security.oauth2.constants.SecurityOAuth2CacheNames;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>OAuth2 会话生命周期服务默认实现</p>
 *
 * @author goya
 * @since 2026/2/25
 */
public class OAuth2SecuritySessionLifecycleService implements SecuritySessionLifecycleService {

    private static final String KEY_SEPARATOR = ":";

    private final OAuth2AuthorizationService authorizationService;
    private final CacheService cacheService;

    public OAuth2SecuritySessionLifecycleService(OAuth2AuthorizationService authorizationService,
                                                 CacheService cacheService) {
        this.authorizationService = authorizationService;
        this.cacheService = cacheService;
    }

    @Override
    public SecuritySessionRevocationResult logoutCurrent(String sid, String tenantId, String userId, String clientId) {
        if (StringUtils.isNotBlank(sid)) {
            return revokeBySid(sid);
        }
        if (StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(clientId)) {
            return revokeByUserAndClient(tenantId, userId, clientId);
        }
        if (StringUtils.isNotBlank(tenantId) && StringUtils.isNotBlank(userId)) {
            return revokeByUser(tenantId, userId);
        }
        return SecuritySessionRevocationResult.empty();
    }

    @Override
    public SecuritySessionRevocationResult revokeBySid(String sid) {
        if (StringUtils.isBlank(sid)) {
            return SecuritySessionRevocationResult.empty();
        }
        Set<String> authorizationIds = loadAuthorizationIds(SecurityOAuth2CacheNames.SSO_SID_AUTHORIZATION_INDEX, sid);
        return revokeAuthorizationSet(authorizationIds);
    }

    @Override
    public SecuritySessionRevocationResult revokeByUser(String tenantId, String userId) {
        if (StringUtils.isAnyBlank(tenantId, userId)) {
            return SecuritySessionRevocationResult.empty();
        }
        String key = composeUserKey(tenantId, userId);
        Set<String> authorizationIds = loadAuthorizationIds(SecurityOAuth2CacheNames.SSO_USER_AUTHORIZATION_INDEX, key);
        return revokeAuthorizationSet(authorizationIds);
    }

    @Override
    public SecuritySessionRevocationResult revokeByUserAndClient(String tenantId, String userId, String clientId) {
        if (StringUtils.isAnyBlank(tenantId, userId, clientId)) {
            return SecuritySessionRevocationResult.empty();
        }
        String key = composeUserClientKey(tenantId, userId, clientId);
        Set<String> authorizationIds = loadAuthorizationIds(SecurityOAuth2CacheNames.SSO_USER_CLIENT_AUTHORIZATION_INDEX, key);
        return revokeAuthorizationSet(authorizationIds);
    }

    private SecuritySessionRevocationResult revokeAuthorizationSet(Set<String> authorizationIds) {
        if (authorizationIds.isEmpty()) {
            return SecuritySessionRevocationResult.empty();
        }
        long revokedCount = 0L;
        for (String authorizationId : authorizationIds) {
            if (StringUtils.isBlank(authorizationId)) {
                continue;
            }
            OAuth2Authorization authorization = authorizationService.findById(authorizationId);
            if (authorization == null) {
                continue;
            }
            authorizationService.remove(authorization);
            revokedCount++;
        }
        return new SecuritySessionRevocationResult(authorizationIds.size(), revokedCount);
    }

    private Set<String> loadAuthorizationIds(String cacheName, String key) {
        String raw = cacheService.get(cacheName, key, String.class);
        return parseValues(raw);
    }

    private Set<String> parseValues(String raw) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        if (StringUtils.isBlank(raw)) {
            return values;
        }
        String[] parts = raw.split(",");
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                values.add(part.trim());
            }
        }
        return values;
    }

    private String composeUserKey(String tenantId, String userId) {
        return tenantId + KEY_SEPARATOR + userId;
    }

    private String composeUserClientKey(String tenantId, String userId, String clientId) {
        return tenantId + KEY_SEPARATOR + userId + KEY_SEPARATOR + clientId;
    }
}
