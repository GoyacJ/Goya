package com.ysmjjsy.goya.component.framework.cache.caffeine;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * <p>基于 Spring CacheManager 的 CacheService 实现（Caffeine）</p>
 *
 * <p>复用同一底层缓存：CacheService 与 @Cacheable 用的是同一个 CacheManager。</p>
 *
 * @author goya
 * @since 2026/1/12 22:54
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineCacheService implements CacheService {

    private final org.springframework.cache.CacheManager cacheManager;

    @Override
    public <T> T get(String cacheName, Object key, Class<T> type) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return null;
        }
        return cache.get(key, type);
    }

    @Override
    public <T> Optional<T> getOptional(String cacheName, Object key, Class<T> type) {
        return Optional.ofNullable(get(cacheName, key, type));
    }

    @Override
    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        cache.put(key, value);
    }

    @Override
    public void put(String cacheName, Object key, Object value, Duration ttl) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }
        if (cache instanceof GoyaCaffeineCache gc) {
            gc.put(key, value, ttl);
            return;
        }
        // 兜底：不支持 per-entry TTL 的 Cache 实现，降级为普通 put
        cache.put(key, value);
    }

    @Override
    public boolean evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return false;
        }
        return cache.evictIfPresent(key);
    }

    @Override
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    public Map<Object, Object> getAll(String cacheName, Collection<?> keys) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null || keys == null || keys.isEmpty()) {
            return Map.of();
        }
        Map<Object, Object> out = new LinkedHashMap<>();
        for (Object k : keys) {
            Cache.ValueWrapper vw = cache.get(k);
            if (vw != null) {
                out.put(k, vw.get());
            }
        }
        return out;
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Supplier<T> loader) {
        return getOrLoad(cacheName, key, type, null, loader);
    }

    @Override
    public <T> T getOrLoad(String cacheName, Object key, Class<T> type, Duration ttl, Supplier<T> loader) {
        Objects.requireNonNull(loader, "loader 不能为空");
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return loader.get();
        }

        T existed = cache.get(key, type);
        if (existed != null) {
            return existed;
        }

        // 简单实现：加载后写入（并发下可能重复加载；若你需要更强收敛，可基于 Caffeine cache.get(key, mappingFunction) 做进一步优化）
        T loaded = loader.get();
        if (ttl != null && cache instanceof GoyaCaffeineCache gc) {
            gc.put(key, loaded, ttl);
        } else {
            cache.put(key, loaded);
        }
        return loaded;
    }
}
