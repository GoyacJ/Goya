package com.ysmjjsy.goya.component.cache.core.constants;

import com.ysmjjsy.goya.component.framework.constants.PropertyConst;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/15 10:52
 */
public interface CacheConst {

    String CACHE_NAME = "cache";

    String CACHE_PREFIX = "cache:";

    String CACHE_KEY_SEPARATOR = ":";

    String CACHE_KEY_DELIMITER = "@";

    String PROPERTY_CACHE = PropertyConst.PROPERTY_GOYA + ".cache";

    String PROPERTY_MULTI_LEVEL = PROPERTY_CACHE + ".multi-level";

    String PROPERTY_CAFFEINE = PROPERTY_CACHE + ".caffeine";

    String PROPERTY_REDIS = PROPERTY_CACHE + ".redis";
}
