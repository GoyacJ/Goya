package com.ysmjjsy.goya.component.mybatisplus.tenant.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>租户路由注解</p>
 *
 * <p>用于非 Web 场景显式触发租户上下文与数据源路由。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantRouting {
}
