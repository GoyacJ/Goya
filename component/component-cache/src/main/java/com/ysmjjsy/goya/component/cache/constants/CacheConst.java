package com.ysmjjsy.goya.component.cache.constants;

import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import com.ysmjjsy.goya.component.framework.constants.PropertyConst;

/**
 * <p>缓存常量定义接口</p>
 * <p>包含缓存相关的配置前缀、默认值、分隔符等常量</p>
 *
 * @author goya
 * @since 2025/12/21 22:51
 */
public interface CacheConst {

    /* ---------- 配置属性前缀 ---------- */
    
    /**
     * 缓存配置前缀
     * 配置示例: platform.cache.type=redis
     */
    String PROPERTY_CACHE = PropertyConst.PROPERTY_PLATFORM + ".cache";

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
