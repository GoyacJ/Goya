package com.ysmjjsy.goya.component.cache.caffeine;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.ysmjjsy.goya.component.cache.core.definition.ICache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.caffeine.CaffeineCache;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 22:22
 */
@Slf4j
public class GoyaCaffeineCache extends CaffeineCache implements ICache<Object, Object>, org.springframework.cache.Cache {

    public GoyaCaffeineCache(String name, Cache<Object, Object> cache) {
        super(name, cache);
    }

    public GoyaCaffeineCache(String name, Cache<Object, Object> cache, boolean allowNullValues) {
        super(name, cache, allowNullValues);
    }

    public GoyaCaffeineCache(String name, AsyncCache<Object, Object> cache, boolean allowNullValues) {
        super(name, cache, allowNullValues);
    }

    @Override
    public void delete(Object key) {
        getNativeCache().invalidate(key);
    }

    @Override
    public boolean exists(Object key) {
        return this.get(key) != null;
    }
}
