package com.ysmjjsy.goya.component.cache.multilevel.factory;

import com.ysmjjsy.goya.component.cache.core.exception.CacheException;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCache;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheOrchestrator;
import com.ysmjjsy.goya.component.cache.multilevel.core.MultiLevelCacheSpec;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCache;
import com.ysmjjsy.goya.component.cache.multilevel.definition.LocalCacheFactory;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCache;
import com.ysmjjsy.goya.component.cache.multilevel.definition.RemoteCacheFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * <p>多级缓存工厂</p>
 * <p>使用 LocalCacheFactory 和 RemoteCacheFactory 创建缓存实例</p>
 * <p>根据配置决定是否启用多级缓存：</p>
 * <ul>
 *     <li>如果只有 L1 或 L2，创建单级缓存</li>
 *     <li>如果两者都有，创建多级缓存</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/15 11:32
 */
@Slf4j
@RequiredArgsConstructor
public class MultiLevelCacheFactory {

    /**
     * 本地缓存工厂（可选）
     */
    private final LocalCacheFactory localCacheFactory;

    /**
     * 远程缓存工厂（可选）
     */
    private final RemoteCacheFactory remoteCacheFactory;

    /**
     * 多级缓存编排器
     */
    private final MultiLevelCacheOrchestrator orchestrator;

    /**
     * 创建缓存实例
     *
     * @param cacheName 缓存名称
     * @param spec      缓存配置规范
     * @return Cache 实例
     */
    @SuppressWarnings("unchecked")
    public Cache createCache(String cacheName, MultiLevelCacheSpec spec) {
        if (cacheName == null || cacheName.isBlank()) {
            throw new IllegalArgumentException("Cache name cannot be null or blank");
        }
        if (spec == null || !spec.isValid()) {
            throw new IllegalArgumentException("Cache spec cannot be null and must be valid");
        }

        LocalCache<Object, Object> localCache = null;
        RemoteCache<Object, Object> remoteCache = null;

        // 创建 L1 本地缓存
        if (localCacheFactory != null) {
            localCache = (LocalCache<Object, Object>) localCacheFactory.create(cacheName, spec);
        }

        // 创建 L2 远程缓存
        if (remoteCacheFactory != null) {
            remoteCache = (RemoteCache<Object, Object>) remoteCacheFactory.create(cacheName, spec);
        }

        // 如果两者都有，创建多级缓存
        if (localCache != null && remoteCache != null) {
            // 注册本地缓存到编排器
            orchestrator.registerLocalCache(cacheName, localCache);

            MultiLevelCache multiLevelCache = new MultiLevelCache(
                    cacheName,
                    localCache,
                    remoteCache,
                    orchestrator,
                    spec
            );

            if (log.isDebugEnabled()) {
                log.debug("Created multi-level cache: cacheName={}", cacheName);
            }

            return multiLevelCache;
        }

        // 如果只有 L1，返回 L1 缓存（需要适配为 Spring Cache）
        if (localCache != null) {
            if (log.isDebugEnabled()) {
                log.debug("Created local-only cache: cacheName={}", cacheName);
            }
            return adaptToSpringCache(cacheName, localCache, spec);
        }

        // 如果只有 L2，返回 L2 缓存（需要适配为 Spring Cache）
        if (remoteCache != null) {
            if (log.isDebugEnabled()) {
                log.debug("Created remote-only cache: cacheName={}", cacheName);
            }
            return adaptToSpringCache(cacheName, remoteCache, spec);
        }

        throw new IllegalStateException("Neither local cache factory nor remote cache factory is available");
    }

    /**
     * 将 ICache 适配为 Spring Cache
     *
     * @param cacheName 缓存名称
     * @param cache     ICache 实例
     * @param spec      缓存配置规范
     * @return Spring Cache 实例
     */
    private Cache adaptToSpringCache(String cacheName, com.ysmjjsy.goya.component.cache.core.definition.ICache<Object, Object> cache, MultiLevelCacheSpec spec) {
        return new Cache() {
            @Override
            @NullMarked
            public String getName() {
                return cacheName;
            }

            @Override
            @NullMarked
            public Object getNativeCache() {
                return cache;
            }

            @Override
            @NullMarked
            public @Nullable ValueWrapper get(Object key) {
                Object value = cache.get(key);
                return value != null ? () -> value : null;
            }

            @Override
            @SuppressWarnings("unchecked")
            @NullMarked
            public <T> T get(Object key, Class<T> type) {
                Object value = cache.get(key);
                if (type.isInstance(value)) {
                    return (T) value;
                }
                return null;
            }

            @Override
            @SuppressWarnings("unchecked")
            @NullMarked
            public <T> T get(Object key, Callable<T> valueLoader) {
                Object value = cache.get(key);
                if (value != null) {
                    return (T) value;
                }
                try {
                    T loadedValue = valueLoader.call();
                    cache.put(key, loadedValue);
                    return loadedValue;
                } catch (Exception e) {
                    throw new CacheException("Failed to load cache value", e);
                }
            }

            @Override
            @NullMarked
            public void put(Object key, Object value) {
                cache.put(key, value);
            }

            @Override
            @NullMarked
            public void evict(Object key) {
                cache.delete(key);
            }

            @Override
            public void clear() {
                // 单级缓存不支持清空操作
                log.warn("Clear operation is not supported for single-level cache: cacheName={}", cacheName);
            }
        };
    }
}
