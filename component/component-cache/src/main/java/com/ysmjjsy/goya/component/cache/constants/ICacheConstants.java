package com.ysmjjsy.goya.component.cache.constants;

import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/21 22:51
 */
public interface ICacheConstants {

    /**
     * 缓存配置前缀
     */
    String PROPERTY_CACHE = IBaseConstants.PROPERTY_PLATFORM + ".cache";

    /* ---------- 通用缓存常量 ---------- */

    String CACHE_PREFIX = "cache:";

    String CACHE_PROPERTIES_PREFIX = CACHE_PREFIX + "properties:";

}
