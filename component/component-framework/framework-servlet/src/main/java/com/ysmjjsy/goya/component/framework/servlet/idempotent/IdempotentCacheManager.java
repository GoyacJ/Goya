package com.ysmjjsy.goya.component.framework.servlet.idempotent;

import com.ysmjjsy.goya.component.framework.cache.support.CacheSupport;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import lombok.Getter;

import static com.ysmjjsy.goya.component.framework.servlet.constant.WebConst.CACHE_WEB_PREFIX;


/**
 * <p>幂等Cache管理</p>
 *
 * @author goya
 * @since 2025/10/9 10:59
 */
@Getter
public class IdempotentCacheManager extends CacheSupport<String, String> {

    public static final String CACHE_IDEMPOTENT_PREFIX = CACHE_WEB_PREFIX + "idempotent:";

    public IdempotentCacheManager(GoyaWebProperties.Idempotent idempotent) {
        super(CACHE_IDEMPOTENT_PREFIX, idempotent.expire());
    }
}
