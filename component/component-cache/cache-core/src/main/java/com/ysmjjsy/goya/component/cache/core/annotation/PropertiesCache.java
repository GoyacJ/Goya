package com.ysmjjsy.goya.component.cache.core.annotation;

import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/23 23:34
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertiesCache {

    /**
     * 缓存 key，默认使用类名
     */
    String cacheKey() default "";

    /**
     * 初始化顺序，值越小越先写入
     */
    int order() default Ordered.LOWEST_PRECEDENCE;
}

