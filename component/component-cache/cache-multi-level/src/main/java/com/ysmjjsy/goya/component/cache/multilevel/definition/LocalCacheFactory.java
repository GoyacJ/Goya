package com.ysmjjsy.goya.component.cache.multilevel.definition;

import com.ysmjjsy.goya.component.cache.core.definition.CacheService;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheSpec;

/**
 * <p>本地缓存工厂</p>
 *
 * @author goya
 * @since 2026/1/15 11:15
 */
public interface LocalCacheFactory extends CacheService {

    /**
     * 创建本地缓存实例
     *
     * @param cacheName 缓存名称
     * @return LocalCache 实例
     */
    LocalCache<?, ?> create(String cacheName, MultiLevelCacheSpec spec);
}
