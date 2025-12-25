package com.ysmjjsy.goya.component.cache.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.ysmjjsy.goya.component.cache.factory.CaffeineFactory;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/25 21:45
 */
@Slf4j
public class LocalCaffeineCacheManager extends CaffeineCacheManager implements CacheManager {

    private final CaffeineFactory caffeineFactory;

    public LocalCaffeineCacheManager(CaffeineFactory caffeineFactory) {
        this.setAllowNullValues(caffeineFactory.getAllowNullValues());
        this.caffeineFactory = caffeineFactory;
    }

    public LocalCaffeineCacheManager(CaffeineFactory caffeineFactory, String... names) {
        super(names);
        this.setAllowNullValues(caffeineFactory.getAllowNullValues());
        this.caffeineFactory = caffeineFactory;
    }

    @Override
    @NullMarked
    protected Cache<Object, Object> createNativeCaffeineCache(String name) {
        return caffeineFactory.create(name).build();
    }
}
