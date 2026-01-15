package com.ysmjjsy.goya.component.cache.multilevel.definition;

import com.ysmjjsy.goya.component.cache.core.definition.CacheService;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheSpec;

/**
 * <p>远程缓存工厂</p>
 *
 * @author goya
 * @since 2026/1/15 11:18
 */
public interface RemoteCacheFactory extends CacheService {

    /**
     * 创建远程缓存实例
     *
     * @param cacheName 缓存名称
     * @param spec      缓存配置规范
     * @return RemoteCache 实例
     */
    RemoteCache<?, ?> create(String cacheName, MultiLevelCacheSpec spec);
}
