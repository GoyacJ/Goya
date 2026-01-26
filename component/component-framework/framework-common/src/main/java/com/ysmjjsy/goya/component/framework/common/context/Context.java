package com.ysmjjsy.goya.component.framework.common.context;

/**
 * <p>context interface</p>
 *
 * @author goya
 * @since 2026/1/7 22:25
 */
public interface Context {

    /**
     * <p>get value from context</p>
     *
     * @param key key
     * @param <T> value type
     * @return value
     */
    <T> T get(ContextKey<T> key);

    /**
     * <p>contains key in context</p>
     *
     * @param key key
     * @return true if contains
     */
    boolean contains(ContextKey<?> key);
}
