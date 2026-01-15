package com.ysmjjsy.goya.component.security.authentication.login;

import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.template.AbstractCounterTemplate;
import com.ysmjjsy.goya.component.cache.ttl.TtlStrategy;
import com.ysmjjsy.goya.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.security.authentication.constants.ISecurityAuthenticationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/4 13:31
 */
@Slf4j
@RequiredArgsConstructor
public class LoginFailureCacheManger extends AbstractCounterTemplate<String> {

    private final SecurityAuthenticationProperties.LoginFailureConfig loginFailureConfig;

    @Override
    protected String getCacheName() {
        return ISecurityAuthenticationConstants.CACHE_SECURITY_AUTHENTICATION_LOGIN_PREFIX;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(loginFailureConfig.expire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }
}
