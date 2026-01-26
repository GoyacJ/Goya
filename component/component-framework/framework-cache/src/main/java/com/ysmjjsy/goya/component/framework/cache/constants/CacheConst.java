package com.ysmjjsy.goya.component.framework.cache.constants;

import com.ysmjjsy.goya.component.framework.common.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.core.constants.PropertyConst;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/15 10:52
 */
public interface CacheConst {

    /* ---------- 配置属性前缀 ---------- */
    String PROPERTY_CACHE = PropertyConst.PROPERTY_GOYA + ".cache";
    String PROPERTY_CAFFEINE = PROPERTY_CACHE + ".caffeine";
    String PROPERTY_REDIS = PROPERTY_CACHE + ".redis";

    /* ---------- 通用缓存常量 ---------- */
    /**
     * 缓存键前缀
     */
    String CACHE_PREFIX = "cache:";

    /**
     * 缓存属性前缀
     */
    String CACHE_PROPERTIES_PREFIX = CACHE_PREFIX + "properties:";


    String CACHE_SECURE_KEY_PREFIX = CACHE_PREFIX + "secure_key:";

    /**
     * 缓存分隔符
     * 用于组合缓存键，例如: cache:user:123
     */
    String CACHE_SEPARATOR = SymbolConst.COLON;
}
