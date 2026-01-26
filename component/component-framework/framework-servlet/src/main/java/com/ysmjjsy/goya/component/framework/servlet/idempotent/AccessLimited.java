package com.ysmjjsy.goya.component.framework.servlet.idempotent;

import java.lang.annotation.*;
import java.time.Duration;

/**
 * <p>接口防刷注解</p>
 *
 * @author goya
 * @since 2025/10/9 09:41
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
public @interface AccessLimited {

    /**
     * 单位时间内同一个接口可以访问的次数
     *
     * @return int
     */
    int maxTimes() default 0;

    /**
     * 持续时间，即在多长时间内，限制访问多少次。具体单位根据TimeUnit的设置而定。
     * <p>
     * 使用Duration格式{@link Duration}
     * <p>
     * 默认为：0，即不设置该属性。那么就使用StampProperies中的配置进行设置。
     * 如果设置了该值，就以该值进行设置。
     */
    String duration() default "";
}
