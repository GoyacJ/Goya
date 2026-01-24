package com.ysmjjsy.goya.component.framework.core.enums;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * <p>枚举值校验注解：校验字段的值是否为指定枚举的合法 code</p>
 *
 * @author goya
 * @since 2026/1/24 15:46
 */
@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValue {

    /**
     * 枚举类型（必须实现 CodeEnum）。
     *
     * @return enum class
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 校验失败的提示 key 或默认文案。
     *
     * <p>建议配合 i18n 使用 key，例如：GOYA-FRAME-VALIDATION-0002</p>
     */
    String message() default "枚举值不合法";

    /** @return groups */
    Class<?>[] groups() default {};

    /** @return payload */
    Class<? extends Payload>[] payload() default {};
}
