package com.ysmjjsy.goya.component.core.context;

/**
 * <p>can write context</p>
 *
 * @author goya
 * @since 2026/1/7 22:32
 */
public interface MutableContext extends Context {

    /**
     * set key
     *
     * @param key   key
     * @param value value
     * @param <T>   value type
     */
    <T> void set(ContextKey<T> key, T value);

    /**
     * remove key
     *
     * @param key key
     */
    void remove(ContextKey<?> key);
}
