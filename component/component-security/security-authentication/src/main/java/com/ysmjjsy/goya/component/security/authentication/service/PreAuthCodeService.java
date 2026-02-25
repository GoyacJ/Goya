package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthCacheNames;
import com.ysmjjsy.goya.component.security.authentication.service.model.PreAuthCodePayload;

import java.time.Duration;
import java.util.Optional;

/**
 * <p>预认证码服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PreAuthCodeService {

    private final CacheService cacheService;
    private final SecurityAuthenticationProperties securityAuthenticationProperties;

    public PreAuthCodeService(CacheService cacheService,
                              SecurityAuthenticationProperties securityAuthenticationProperties) {
        this.cacheService = cacheService;
        this.securityAuthenticationProperties = securityAuthenticationProperties;
    }

    public String issue(PreAuthCodePayload payload) {
        String preAuthCode = securityAuthenticationProperties.preAuthCodePrefix() + GoyaIdUtils.fastSimpleUUID();
        String encodedPayload = GoyaJson.toJson(payload);
        cacheService.put(
                SecurityAuthCacheNames.PRE_AUTH_CODE,
                preAuthCode,
                encodedPayload,
                securityAuthenticationProperties.preAuthCodeTtl()
        );
        return preAuthCode;
    }

    public Optional<PreAuthCodePayload> consume(String preAuthCode) {
        if (preAuthCode == null || preAuthCode.isBlank()) {
            return Optional.empty();
        }

        if (cacheService.exists(SecurityAuthCacheNames.PRE_AUTH_CODE_CONSUMED, preAuthCode)) {
            return Optional.empty();
        }

        Duration lockTtl = resolveProcessingLockTtl();
        boolean acquired = cacheService.putIfAbsent(
                SecurityAuthCacheNames.PRE_AUTH_CODE_PROCESSING,
                preAuthCode,
                "1",
                lockTtl
        );
        if (!acquired) {
            return Optional.empty();
        }

        try {
            String encodedPayload = cacheService.get(SecurityAuthCacheNames.PRE_AUTH_CODE, preAuthCode, String.class);
            if (encodedPayload == null || encodedPayload.isBlank()) {
                return Optional.empty();
            }

            boolean markedConsumed = cacheService.putIfAbsent(
                    SecurityAuthCacheNames.PRE_AUTH_CODE_CONSUMED,
                    preAuthCode,
                    "1",
                    securityAuthenticationProperties.preAuthCodeTtl()
            );
            if (!markedConsumed) {
                return Optional.empty();
            }

            cacheService.delete(SecurityAuthCacheNames.PRE_AUTH_CODE, preAuthCode);
            return Optional.ofNullable(GoyaJson.fromJson(encodedPayload, PreAuthCodePayload.class));
        } finally {
            cacheService.delete(SecurityAuthCacheNames.PRE_AUTH_CODE_PROCESSING, preAuthCode);
        }
    }

    public Optional<PreAuthCodePayload> read(String preAuthCode) {
        if (preAuthCode == null || preAuthCode.isBlank()) {
            return Optional.empty();
        }
        String encodedPayload = cacheService.get(SecurityAuthCacheNames.PRE_AUTH_CODE, preAuthCode, String.class);
        if (encodedPayload == null || encodedPayload.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(GoyaJson.fromJson(encodedPayload, PreAuthCodePayload.class));
    }

    private Duration resolveProcessingLockTtl() {
        long maxSeconds = Math.max(5L, securityAuthenticationProperties.preAuthCodeTtl().toSeconds());
        long lockSeconds = Math.min(maxSeconds, 30L);
        return Duration.ofSeconds(lockSeconds);
    }
}
