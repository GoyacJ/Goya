package com.ysmjjsy.goya.component.social.cache;

import com.ysmjjsy.goya.component.framework.cache.support.CacheSupport;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaRandomUtils;
import com.ysmjjsy.goya.component.social.configuration.properties.SocialProperties;
import com.ysmjjsy.goya.component.social.constants.ISocialConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 22:45
 */
@Slf4j
public class SmsCheckCacheManager extends CacheSupport<String, String> {

    @Getter
    private final SocialProperties.Sms sms;

    public SmsCheckCacheManager(SocialProperties.Sms sms) {
        super(ISocialConstants.CACHE_SMS_CODE, sms.expire());
        this.sms = sms;
    }

    public String generateValue() {
        if (Boolean.TRUE.equals(sms.sandbox())) {
            return sms.testCode();
        } else {
            return GoyaRandomUtils.randomNumberString(sms.length());
        }
    }
}
