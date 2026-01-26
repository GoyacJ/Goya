package com.ysmjjsy.goya.component.framework.log.aop;

import java.lang.annotation.*;

/**
 * <p>方法日志注解：用于显式启用/覆盖日志策略。</p>
 *
 * <p>若未配置 includePackages，则仅拦截带本注解的方法。</p>
 * <p>若配置了 includePackages，则默认拦截包内方法；本注解可用于“覆盖开关”。</p>
 *
 * @author goya
 * @since 2026/1/24 21:59
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Loggable {

    /**
     * 是否记录入参（默认使用全局配置）。
     */
    boolean logArgs() default true;

    /**
     * 是否记录返回值（默认使用全局配置）。
     */
    boolean logResult() default false;

    /**
     * 是否启用脱敏（默认启用）。
     */
    boolean mask() default true;
}