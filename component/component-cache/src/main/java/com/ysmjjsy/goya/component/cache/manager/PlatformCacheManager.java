package com.ysmjjsy.goya.component.cache.manager;

import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/25 22:55
 */
public class PlatformCacheManager implements CacheManager {

    @Override
    public @Nullable Cache getCache(String name) {
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return List.of();
    }
}
