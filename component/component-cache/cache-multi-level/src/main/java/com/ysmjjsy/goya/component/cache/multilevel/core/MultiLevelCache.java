package com.ysmjjsy.goya.component.cache.multilevel.core;

import com.ysmjjsy.goya.component.cache.core.definition.ICache;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.support.AbstractValueAdaptingCache;

import java.util.concurrent.Callable;

/**
 * <p>多级缓存</p>
 *
 * @author goya
 * @since 2026/1/15 11:26
 */
@Slf4j
public class MultiLevelCache<K,V> extends AbstractValueAdaptingCache implements ICache<K,V> {

    /**
     * Create an {@code AbstractValueAdaptingCache} with the given setting.
     *
     * @param allowNullValues whether to allow for {@code null} values
     */
    protected MultiLevelCache(boolean allowNullValues) {
        super(allowNullValues);
    }

    @Override
    public void delete(K key) {

    }

    @Override
    public boolean exists(K key) {
        return false;
    }

    @Override
    protected @Nullable Object lookup(Object key) {
        return null;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Object getNativeCache() {
        return null;
    }

    @Override
    public @Nullable <T> T get(Object key, Callable<T> valueLoader) {
        return null;
    }

    @Override
    public void put(Object key, @Nullable Object value) {

    }

    @Override
    public void evict(Object key) {

    }

    @Override
    public void clear() {

    }
}
