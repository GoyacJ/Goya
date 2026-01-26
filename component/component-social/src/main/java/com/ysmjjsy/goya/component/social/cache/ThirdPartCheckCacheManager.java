package com.ysmjjsy.goya.component.social.cache;

import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.core.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.social.configuration.properties.SocialProperties;
import com.ysmjjsy.goya.component.social.constants.ISocialConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ThirdPartCheckCacheManager extends AbstractCheckTemplate<String, String> implements AuthStateCache {

    @Getter
    private final SocialProperties.ThirdPart thirdPart;

    @Override
    protected String nextValue(String key) {
        return GoyaIdUtils.fastSimpleUUID();
    }

    @Override
    protected String getCacheName() {
        return ISocialConstants.CACHE_THIRD_PARTY;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(thirdPart.timeout());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }

    @Override
    public void cache(String key, String value) {
        this.put(key, value);
    }

    @Override
    public void cache(String key, String value, long expire) {
        this.put(key,value, Duration.ofMillis(expire));
    }

    @Override
    public boolean containsKey(String key) {
        return this.exists(key);
    }
}
