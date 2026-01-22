package com.ysmjjsy.goya.component.security.authentication.service.impl;

import com.ysmjjsy.goya.component.cache.multilevel.service.MultiLevelCacheService;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import java.time.Duration;
import java.util.UUID;

/**
 * <p>基于Redis的OAuth2授权服务实现</p>
 * <p>实现全无状态设计，支持水平扩展</p>
 * <p>存储结构：</p>
 * <ul>
 *   <li>主授权记录：oauth2:authorization:{id} -> OAuth2Authorization (JSON序列化)</li>
 *   <li>授权码映射：oauth2:code:{code} -> {id} (字符串，TTL: 5分钟)</li>
 *   <li>Refresh Token映射：oauth2:refresh:{token} -> {id} (字符串，TTL: 30天)</li>
 * </ul>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>支持多节点部署（无状态）</li>
 *   <li>支持水平扩展</li>
 *   <li>自动过期清理（基于TTL）</li>
 *   <li>高性能（Redis内存存储）</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class CacheOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final MultiLevelCacheService cacheService;

    // Redis Key 前缀
    private static final String AUTHORIZATION_KEY_PREFIX = "oauth2:authorization:";
    private static final String AUTHORIZATION_CODE_KEY_PREFIX = "oauth2:code:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "oauth2:refresh:";

    // 缓存名称
    private static final String CACHE_NAME = "oauth2";

    // 过期时间配置
    private static final Duration AUTHORIZATION_TTL = Duration.ofDays(30); // 与Refresh Token一致
    private static final Duration CODE_TTL = Duration.ofSeconds(300); // 5分钟
    private static final Duration REFRESH_TOKEN_TTL = Duration.ofDays(30);

    @Override
    public void save(OAuth2Authorization authorization) {
        String id = authorization.getId();
        if (id == null) {
            // 如果没有ID，生成一个
            id = UUID.randomUUID().toString();
            // 注意：OAuth2Authorization是不可变的，无法直接设置ID
            // 这里需要重新构建一个带ID的授权对象
            log.warn("[Goya] |- security [authentication] OAuth2Authorization has no ID, generating new ID: {}", id);
        }

        String key = AUTHORIZATION_KEY_PREFIX + id;

        try {
            // 1. 序列化并保存主授权记录
            String value = GoyaJson.toJson(authorization);
            cacheService.put(CACHE_NAME, key, value, AUTHORIZATION_TTL);

            // 2. 如果存在Authorization Code，建立映射关系（短生命周期）
            OAuth2Authorization.Token<OAuth2AuthorizationCode> authCode =
                    authorization.getToken(OAuth2AuthorizationCode.class);
            if (authCode != null && authCode.getToken() != null) {
                String codeValue = authCode.getToken().getTokenValue();
                String codeKey = AUTHORIZATION_CODE_KEY_PREFIX + codeValue;
                cacheService.put(CACHE_NAME, codeKey, id, CODE_TTL);
                log.debug("[Goya] |- security [authentication] Authorization code mapping saved: {} -> {}", codeKey, id);
            }

            // 3. 如果存在Refresh Token，建立映射关系（长生命周期）
            OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                    authorization.getToken(OAuth2RefreshToken.class);
            if (refreshToken != null && refreshToken.getToken() != null) {
                String refreshTokenValue = refreshToken.getToken().getTokenValue();
                String refreshKey = REFRESH_TOKEN_KEY_PREFIX + refreshTokenValue;
                cacheService.put(CACHE_NAME, refreshKey, id, REFRESH_TOKEN_TTL);
                log.debug("[Goya] |- security [authentication] Refresh token mapping saved: {} -> {}", refreshKey, id);
            }

            log.trace("[Goya] |- security [authentication] OAuth2Authorization saved: {}", id);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to save OAuth2Authorization: {}", id, e);
            throw new org.springframework.security.oauth2.core.OAuth2AuthenticationException(
                    new org.springframework.security.oauth2.core.OAuth2Error(
                            org.springframework.security.oauth2.core.OAuth2ErrorCodes.SERVER_ERROR,
                            "授权保存失败",
                            null),
                    e);
        }
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        String id = authorization.getId();
        if (id == null) {
            log.warn("[Goya] |- security [authentication] Cannot remove OAuth2Authorization with null ID");
            return;
        }

        String key = AUTHORIZATION_KEY_PREFIX + id;

        try {
            // 删除主授权记录
            cacheService.delete(CACHE_NAME, key);

            // 删除关联的Code映射
            OAuth2Authorization.Token<OAuth2AuthorizationCode> authCode =
                    authorization.getToken(OAuth2AuthorizationCode.class);
            if (authCode != null && authCode.getToken() != null) {
                String codeKey = AUTHORIZATION_CODE_KEY_PREFIX + authCode.getToken().getTokenValue();
                cacheService.delete(CACHE_NAME, codeKey);
            }

            // 删除关联的Refresh Token映射
            OAuth2Authorization.Token<OAuth2RefreshToken> refreshToken =
                    authorization.getToken(OAuth2RefreshToken.class);
            if (refreshToken != null && refreshToken.getToken() != null) {
                String refreshKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken.getToken().getTokenValue();
                cacheService.delete(CACHE_NAME, refreshKey);
            }

            log.trace("[Goya] |- security [authentication] OAuth2Authorization removed: {}", id);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to remove OAuth2Authorization: {}", id, e);
        }
    }

    @Override
    public OAuth2Authorization findById(String id) {
        if (id == null) {
            return null;
        }

        String key = AUTHORIZATION_KEY_PREFIX + id;
        String value = cacheService.get(CACHE_NAME, key);

        if (value == null) {
            return null;
        }

        try {
            return GoyaJson.fromJson(value, OAuth2Authorization.class);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to deserialize OAuth2Authorization: {}", id, e);
            return null;
        }
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        if (token == null || tokenType == null) {
            return null;
        }

        String id = null;

        if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
            // 通过Authorization Code查找
            String codeKey = AUTHORIZATION_CODE_KEY_PREFIX + token;
            id = cacheService.get(CACHE_NAME, codeKey);
            if (id != null) {
                // 删除Code映射（一次性使用）
                cacheService.delete(CACHE_NAME, codeKey);
                log.debug("[Goya] |- security [authentication] Authorization code used and removed: {}", codeKey);
            }
        } else if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
            // 通过Refresh Token查找
            String refreshKey = REFRESH_TOKEN_KEY_PREFIX + token;
            id = cacheService.get(CACHE_NAME, refreshKey);
        }
        // Access Token是JWT，不需要查找（自包含）

        if (id == null) {
            return null;
        }

        return findById(id);
    }
}

