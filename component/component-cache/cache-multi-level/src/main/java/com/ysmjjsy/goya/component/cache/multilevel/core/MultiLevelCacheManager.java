package com.ysmjjsy.goya.component.cache.multilevel.core;

import com.ysmjjsy.goya.component.cache.multilevel.configuration.properties.MultiLevelProperties;
import com.ysmjjsy.goya.component.cache.multilevel.factory.MultiLevelCacheFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;
import java.util.Collections;

/**
 * <p>多级缓存管理器</p>
 * <p>继承 AbstractCacheManager，使用 MultiLevelCacheFactory 创建缓存实例</p>
 * <p>支持动态创建缓存</p>
 *
 * @author goya
 * @since 2026/1/15 11:21
 */
@Slf4j
@RequiredArgsConstructor
public class MultiLevelCacheManager extends AbstractCacheManager {

    /**
     * 多级缓存工厂
     */
    private final MultiLevelCacheFactory cacheFactory;

    /**
     * 多级缓存配置属性
     */
    private final MultiLevelProperties properties;

    @Override
    @NullMarked
    protected Collection<? extends Cache> loadCaches() {
        // 支持动态创建缓存，不需要预加载
        return Collections.emptyList();
    }

    @Override
    protected Cache getMissingCache(String name) {
        if (name.isBlank()) {
            return null;
        }

        try {
            // 构建缓存配置规范
            MultiLevelCacheSpec spec = properties.buildSpec(name);

            // 使用工厂创建缓存实例
            Cache cache = cacheFactory.createCache(name, spec);

            if (log.isDebugEnabled()) {
                log.debug("Created cache dynamically: cacheName={}", name);
            }

            return cache;
        } catch (Exception e) {
            log.error("Failed to create cache: cacheName={}", name, e);
            return null;
        }
    }
}
