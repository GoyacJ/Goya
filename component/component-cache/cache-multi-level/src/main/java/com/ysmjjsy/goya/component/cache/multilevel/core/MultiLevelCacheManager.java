package com.ysmjjsy.goya.component.cache.multilevel.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;
import java.util.List;

/**
 * <p>多级缓存管理器</p>
 *
 * @author goya
 * @since 2026/1/15 11:21
 */
@Slf4j
@RequiredArgsConstructor
public class MultiLevelCacheManager extends AbstractCacheManager {

    @Override
    @NullMarked
    protected Collection<? extends Cache> loadCaches() {
        return List.of();
    }
}
