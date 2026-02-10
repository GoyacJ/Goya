package com.ysmjjsy.goya.component.security.oauth2.consent;

import com.ysmjjsy.goya.component.cache.multilevel.service.MultiLevelCacheService;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>基于Redis的用户授权同意服务实现</p>
 * <p>存储用户授权同意记录到Redis</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@RequiredArgsConstructor
public class CacheConsentService implements ConsentService {

    private final MultiLevelCacheService cacheService;
    private static final String CACHE_NAME = "oauth2_consent";
    private static final String KEY_PREFIX = "consent:";
    private static final String USER_CLIENTS_KEY_PREFIX = "consent:user_clients:";
    private static final Duration CONSENT_EXPIRY = Duration.ofDays(365);

    @Override
    public boolean isConsentGiven(String userId, String clientId, Set<String> scopes) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(clientId)) {
            return false;
        }

        String key = buildKey(userId, clientId);
        String consentData = cacheService.get(CACHE_NAME, key);

        if (StringUtils.isBlank(consentData)) {
            return false;
        }

        try {
            Set<String> consentedScopes = GoyaJson.fromJsonSet(consentData, String.class);
            if (consentedScopes == null || consentedScopes.isEmpty()) {
                return false;
            }

            // 检查请求的范围是否都在已授权范围内
            return consentedScopes.containsAll(scopes);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to parse consent data for key: {}", key, e);
            return false;
        }
    }

    @Override
    public void saveConsent(String userId, String clientId, Set<String> scopes) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(clientId) || scopes == null || scopes.isEmpty()) {
            return;
        }

        String key = buildKey(userId, clientId);
        String consentData = GoyaJson.toJson(scopes);

        cacheService.put(CACHE_NAME, key, consentData, CONSENT_EXPIRY);
        
        // 维护用户的所有客户端ID列表
        addClientToUserList(userId, clientId);
        
        log.debug("[Goya] |- security [authentication] Consent saved for user: {} | client: {} | scopes: {}", 
                userId, clientId, scopes);
    }

    @Override
    public void revokeConsent(String userId, String clientId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(clientId)) {
            return;
        }

        String key = buildKey(userId, clientId);
        cacheService.delete(CACHE_NAME, key);
        
        // 从用户客户端列表中移除
        removeClientFromUserList(userId, clientId);
        
        log.debug("[Goya] |- security [authentication] Consent revoked for user: {} | client: {}", userId, clientId);
    }

    @Override
    public Set<String> getConsentedScopes(String userId, String clientId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(clientId)) {
            return new HashSet<>();
        }

        String key = buildKey(userId, clientId);
        String consentData = cacheService.get(CACHE_NAME, key);

        if (StringUtils.isBlank(consentData)) {
            return new HashSet<>();
        }

        try {
            Set<String> scopes = GoyaJson.fromJsonSet(consentData, String.class);
            return scopes != null ? scopes : new HashSet<>();
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to parse consent data for key: {}", key, e);
            return new HashSet<>();
        }
    }

    @Override
    public Map<String, Set<String>> findAllConsents(String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyMap();
        }

        // 1. 获取用户的所有客户端ID列表
        Set<String> clientIds = getUserClientIds(userId);
        if (clientIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2. 批量查询所有客户端的授权范围
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (String clientId : clientIds) {
            Set<String> scopes = getConsentedScopes(userId, clientId);
            if (!scopes.isEmpty()) {
                result.put(clientId, scopes);
            }
        }

        return result;
    }

    /**
     * 添加客户端到用户客户端列表
     *
     * @param userId   用户ID
     * @param clientId 客户端ID
     */
    private void addClientToUserList(String userId, String clientId) {
        String userClientsKey = USER_CLIENTS_KEY_PREFIX + userId;
        Set<String> clientIds = getUserClientIds(userId);
        if (!clientIds.contains(clientId)) {
            clientIds.add(clientId);
            String clientIdsJson = GoyaJson.toJson(clientIds);
            cacheService.put(CACHE_NAME, userClientsKey, clientIdsJson, CONSENT_EXPIRY);
        }
    }

    /**
     * 从用户客户端列表中移除客户端
     *
     * @param userId   用户ID
     * @param clientId 客户端ID
     */
    private void removeClientFromUserList(String userId, String clientId) {
        String userClientsKey = USER_CLIENTS_KEY_PREFIX + userId;
        Set<String> clientIds = getUserClientIds(userId);
        if (clientIds.remove(clientId)) {
            if (clientIds.isEmpty()) {
                cacheService.delete(CACHE_NAME, userClientsKey);
            } else {
                String clientIdsJson = GoyaJson.toJson(clientIds);
                cacheService.put(CACHE_NAME, userClientsKey, clientIdsJson, CONSENT_EXPIRY);
            }
        }
    }

    /**
     * 获取用户的所有客户端ID列表
     *
     * @param userId 用户ID
     * @return 客户端ID集合
     */
    private Set<String> getUserClientIds(String userId) {
        String userClientsKey = USER_CLIENTS_KEY_PREFIX + userId;
        String clientIdsJson = cacheService.get(CACHE_NAME, userClientsKey, String.class);
        
        if (StringUtils.isBlank(clientIdsJson)) {
            return new HashSet<>();
        }

        try {
            Set<String> clientIds = GoyaJson.fromJsonSet(clientIdsJson, String.class);
            return clientIds != null ? clientIds : new HashSet<>();
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to parse user client IDs for key: {}", userClientsKey, e);
            return new HashSet<>();
        }
    }

    /**
     * 构建Redis Key
     *
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @return Redis Key
     */
    private String buildKey(String userId, String clientId) {
        return KEY_PREFIX + userId + ":" + clientId;
    }
}

