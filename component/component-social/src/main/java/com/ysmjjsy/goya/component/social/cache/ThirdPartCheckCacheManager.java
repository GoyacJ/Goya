package com.ysmjjsy.goya.component.social.cache;

import com.ysmjjsy.goya.component.framework.cache.support.CacheSupport;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.social.configuration.properties.SocialProperties;
import com.ysmjjsy.goya.component.social.constants.ISocialConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.cache.AuthStateCache;

import java.time.Duration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 22:45
 */
@Slf4j
public class ThirdPartCheckCacheManager extends CacheSupport<String, String> implements AuthStateCache {

    @Getter
    private final SocialProperties.ThirdPart thirdPart;

    public ThirdPartCheckCacheManager(SocialProperties.ThirdPart thirdPart) {
        super(ISocialConstants.CACHE_THIRD_PARTY, thirdPart.timeout());
        this.thirdPart = thirdPart;
    }

    protected String generateValue(String key) {
        return GoyaIdUtils.fastSimpleUUID();
    }

    @Override
    public void cache(String key, String value) {
        this.put(key, value);
    }

    @Override
    public void cache(String key, String value, long expire) {
        this.put(key, value, Duration.ofMillis(expire));
    }

    @Override
    public boolean containsKey(String key) {
        return this.exists(key);
    }
}
