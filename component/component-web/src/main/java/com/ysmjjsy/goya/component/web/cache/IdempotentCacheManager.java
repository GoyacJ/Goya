package com.ysmjjsy.goya.component.web.cache;

import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.core.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.web.configuration.properties.WebProperties;
import com.ysmjjsy.goya.component.web.constants.IWebConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * <p>幂等Stamp管理</p>
 *
 * @author goya
 * @since 2025/10/9 10:59
 */
@Getter
@RequiredArgsConstructor
public class IdempotentCacheManager extends AbstractCheckTemplate<String, String> {

    private final WebProperties.Idempotent idempotent;

    @Override
    public String nextValue(String key) {
        return GoyaIdUtils.fastSimpleUUID();
    }

    @Override
    protected String getCacheName() {
        return IWebConstants.CACHE_IDEMPOTENT_PREFIX;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(idempotent.expire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }
}
