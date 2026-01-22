package com.ysmjjsy.goya.component.security.authentication.token;

import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthenticationConst;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * <p>Token黑名单签章</p>
 *
 * @author goya
 * @since 2025/12/17 22:41
 */
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistStamp extends AbstractCheckTemplate<String, String> {

    private final SecurityAuthenticationProperties.TokenBlackListConfig tokenBlackListConfig;

    @Override
    protected String nextValue(String key) {
        return tokenBlackListConfig.defaultReason();
    }

    @Override
    protected String getCacheName() {
        return SecurityAuthenticationConst.CACHE_SECURITY_AUTHENTICATION_TOKEN_BLACK_LIST_PREFIX;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(tokenBlackListConfig.tokenBlackListExpire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }

    /**
     * 将Token加入黑名单（使用默认过期时间）
     *
     * @param token Token值（JWT JTI或Opaque Token）
     */
    public void addToBlacklist(String token) {
        addToBlacklist(token, tokenBlackListConfig.defaultReason(), tokenBlackListConfig.tokenBlackListExpire());
    }

    /**
     * 将Token加入黑名单（使用自定义过期时间）
     *
     * @param token  Token值
     * @param expire 过期时间
     */
    public void addToBlacklist(String token, Duration expire) {
        addToBlacklist(token, tokenBlackListConfig.defaultReason(), expire);
    }

    /**
     * 将Token加入黑名单（完整参数）
     *
     * @param token  Token值
     * @param reason 原因
     * @param expire 过期时间
     */
    public void addToBlacklist(String token, String reason, Duration expire) {
        if (token == null) {
            log.warn("[Goya] |- security [authentication] Cannot add null token to blacklist");
            return;
        }

        put(token, reason, expire);
        log.debug("[Goya] |- security [authentication] Token added to blacklist: {}, reason: {}, expire: {}", token, reason, expire);
    }

    /**
     * 检查Token是否在黑名单中
     *
     * @param token Token值
     * @return true如果在黑名单中，false如果不在
     */
    public boolean isBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        return exists(token);
    }

    /**
     * 从黑名单移除Token（通常不需要，因为Token会自动过期）
     *
     * @param token Token值
     */
    public void removeFromBlacklist(String token) {
        if (token == null) {
            return;
        }

        evict(token);
        log.debug("[Goya] |- security [authentication] Token removed from blacklist: {}", token);
    }
}
