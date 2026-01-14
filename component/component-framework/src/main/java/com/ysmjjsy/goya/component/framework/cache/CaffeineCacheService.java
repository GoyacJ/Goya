package com.ysmjjsy.goya.component.framework.cache;

import com.ysmjjsy.goya.component.core.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * <p>Caffeine Cache Servuce</p>
 *
 * @author goya
 * @since 2026/1/12 22:54
 */
@Slf4j
@RequiredArgsConstructor
public class CaffeineCacheService implements CacheService {

    private final CaffeineCacheManager caffeineCacheManager;

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> V get(String cacheName, K key) {
        org.springframework.cache.Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache {} not found", cacheName);
            return null;
        }
        return (V) cache.get(key, Object.class);
    }

    @Override
    public <K, V> void put(String cacheName, K key, V value) {
        org.springframework.cache.Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache {} not found", cacheName);
            return;
        }
        cache.put(key, value);
    }

    @Override
    public <K> void delete(String cacheName, K key) {
        org.springframework.cache.Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache {} not found", cacheName);
            return;
        }
        cache.evict(key);
    }

    @Override
    public <K> boolean exists(String cacheName, K key) {
        org.springframework.cache.Cache cache = caffeineCacheManager.getCache(cacheName);
        if (cache == null) {
            log.warn("Cache {} not found", cacheName);
            return false;
        }
        return cache.get(key) != null;
    }
}
