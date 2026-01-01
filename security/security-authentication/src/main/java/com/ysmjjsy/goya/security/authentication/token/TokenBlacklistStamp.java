package com.ysmjjsy.goya.security.authentication.token;

import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.ttl.TtlStrategy;
import com.ysmjjsy.goya.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>Token黑名单签章</p>
 *
 * @author goya
 * @since 2025/12/17 22:41
 */
@Slf4j
public class TokenBlacklistStamp extends AbstractCheckTemplate<String, String> {

    private final SecurityAuthenticationProperties.TokenBlackListConfig tokenBlackListConfig;

    public TokenBlacklistStamp(SecurityAuthenticationProperties.TokenBlackListConfig tokenBlackListConfig) {
        this.tokenBlackListConfig = tokenBlackListConfig;
    }

    @Override
    protected String nextValue(String key) {
        return tokenBlackListConfig.defaultReason();
    }

    @Override
    protected String getCacheName() {
        return ISecurityAuthenticationConstants.CACHE_SECURITY_AUTHENTICATION_TOKEN_BLACK_LIST_PREFIX;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(tokenBlackListConfig.tokenBlackListExpire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }
}
