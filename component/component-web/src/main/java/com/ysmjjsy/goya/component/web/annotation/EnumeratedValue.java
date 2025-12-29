package com.ysmjjsy.goya.component.web.annotation;

import com.ysmjjsy.goya.component.web.validation.EnumeratedValueValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * <p>枚举值校验注解</p>
 *
 * @author goya
 * @since 2025/10/9 09:42
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EnumeratedValue.List.class)
@Documented
@Constraint(validatedBy = {EnumeratedValueValidator.class})
public @interface EnumeratedValue {

    // 默认错误消息
    String message() default "必须为指定值";

    String[] names() default {};

    int[] ordinals() default {};

    // 分组
    Class<?>[] groups() default {};

    // 负载
    Class<? extends Payload>[] payload() default {};

    // 指定多个时使用
    @Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER, ElementType.TYPE_USE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        EnumeratedValue[] value();
    }
}
