package com.ysmjjsy.goya.component.framework.masker.annotation;

import java.lang.annotation.*;

/**
 * <p>脱敏标记注解</p>
 * <p>用于标记某个方法返回值或某个类型需要进行脱敏输出。</p>
 *
 * <p>建议用法：</p>
 * <ul>
 *   <li>标记在 Controller 方法：该接口响应体统一脱敏</li>
 *   <li>标记在 DTO 类：该类型输出时默认脱敏（需要集成层支持）</li>
 * </ul>
 * @author goya
 * @since 2026/1/24 22:53
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Mask {

    /**
     * 是否启用脱敏（便于局部关闭）。
     *
     * @return enabled
     */
    boolean value() default true;
}