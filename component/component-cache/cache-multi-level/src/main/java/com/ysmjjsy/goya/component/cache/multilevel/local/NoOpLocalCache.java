package com.ysmjjsy.goya.component.cache.multilevel.local;

import com.ysmjjsy.goya.component.cache.multilevel.core.LocalCache;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.springframework.cache.Cache;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * 空操作本地缓存实现
 *
 * <p>用于支持 L2_ONLY 缓存模式，所有操作都是空操作（不执行任何实际缓存操作）。
 *
 * <p><b>职责：</b>
 * <ul>
 *   <li>实现 {@link LocalCache} 接口的所有方法</li>
 *   <li>所有方法返回 null 或空集合，不执行任何实际操作</li>
 *   <li>用于 L2_ONLY 模式，避免 GoyaCache 需要判断 L1 是否存在</li>
 * </ul>
 *
 * <p><b>使用场景：</b>
 * <ul>
 *   <li>L2_ONLY 缓存模式：只使用远程缓存，不需要本地缓存</li>
 *   <li>测试场景：需要模拟无本地缓存的环境</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/26
 */
@Slf4j
public class NoOpLocalCache implements LocalCache {

    private final String name;

    /**
     * 构造函数
     *
     * @param name 缓存名称
     */
    public NoOpLocalCache(String name) {
        this.name = name;
    }

    @Override
    @NullMarked
    public String getName() {
        return name;
    }

    @Override
    @NullMarked
    public Object getNativeCache() {
        // 返回一个空对象，表示没有实际的缓存实现
        return Collections.emptyMap();
    }

    @Override
    public Cache.ValueWrapper get(@NonNull Object key) {
        // 空操作：总是返回 null（未命中）
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, Class<T> type) {
        // 空操作：总是返回 null
        return null;
    }

    @Override
    public <T> T get(@NonNull Object key, @NonNull Callable<T> valueLoader) {
        // 空操作：直接执行 valueLoader，不缓存结果
        try {
            return valueLoader.call();
        } catch (Exception e) {
            throw new Cache.ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(@NonNull Object key, Object value) {
        // 空操作：不执行任何操作
    }

    @Override
    public void put(@NonNull Object key, Object value, Duration ttl) {
        // 空操作：不执行任何操作
    }

    @Override
    public void evict(@NonNull Object key) {
        // 空操作：不执行任何操作
    }

    @Override
    public void clear() {
        // 空操作：不执行任何操作
    }

    @Override
    public Map<Object, Cache.ValueWrapper> getAll(@NonNull Set<Object> keys) {
        // 空操作：返回空 Map
        return Collections.emptyMap();
    }

    @Override
    public void putAll(@NonNull Map<Object, Object> entries, Duration ttl) {
        // 空操作：不执行任何操作
    }

    // ========== 原子操作 ==========

    @Override
    public long increment(Object key) {
        // 空操作：返回 0（表示没有实际计数）
        return 0L;
    }

    @Override
    public long incrementBy(Object key, long delta) {
        // 空操作：返回 0（表示没有实际计数）
        return 0L;
    }

    @Override
    public long decrement(Object key) {
        // 空操作：返回 0（表示没有实际计数）
        return 0L;
    }

    @Override
    public boolean expire(Object key, Duration ttl) {
        // 空操作：返回 false（key 不存在）
        return false;
    }

    @Override
    public boolean isNoOp() {
        return true;
    }
}

