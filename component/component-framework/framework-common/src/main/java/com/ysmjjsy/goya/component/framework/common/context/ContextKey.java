package com.ysmjjsy.goya.component.framework.common.context;

import java.io.Serializable;

/**
 * <p>context key</p>
 *
 * @author goya
 * @since 2026/1/7 22:23
 */
public record ContextKey<T>(
        /*
          key name
         */
        String name,
        /*
          key type
         */
        Class<T> type
) implements Serializable {
    public static <T> ContextKey<T> of(String name, Class<T> type) {
        return new ContextKey<>(name, type);
    }
}
