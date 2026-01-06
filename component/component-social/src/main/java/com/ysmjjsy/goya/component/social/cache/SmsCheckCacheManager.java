package com.ysmjjsy.goya.component.social.cache;

import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.common.utils.RandomUtils;
import com.ysmjjsy.goya.component.social.configuration.properties.SocialProperties;
import com.ysmjjsy.goya.component.social.constants.ISocialConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 22:45
 */
@Slf4j
@RequiredArgsConstructor
public class SmsCheckCacheManager extends AbstractCheckTemplate<String, String> {

    @Getter
    private final SocialProperties.Sms sms;

    @Override
    protected String nextValue(String key) {
        if (Boolean.TRUE.equals(sms.sandbox())) {
            return sms.testCode();
        } else {
            return RandomUtils.randomNumberString(sms.length());
        }
    }

    @Override
    protected String getCacheName() {
        return ISocialConstants.CACHE_SMS_CODE;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(sms.expire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }
}
