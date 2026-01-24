package com.ysmjjsy.goya.component.framework.cache.caffeine;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.caffeine.CaffeineCacheManager;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/12 22:34
 */
@Slf4j
@RequiredArgsConstructor
public class GoyaCaffeineCacheManager extends CaffeineCacheManager {

    private final CaffeineFactory caffeineFactory;

    @Override
    @NullMarked
    protected Cache<Object, Object> createNativeCaffeineCache(String name) {
        return caffeineFactory.createCaffeineCache(name);
    }

    @Override
    @NullMarked
    protected org.springframework.cache.Cache createCaffeineCache(String name) {
        return caffeineFactory.createSpringCache(name);
    }
}
