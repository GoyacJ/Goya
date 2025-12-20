package com.ysmjjsy.goya.component.common.annotation;

import com.ysmjjsy.goya.component.common.condiition.LocaleCondition;
import com.ysmjjsy.goya.component.common.enums.LocaleEnum;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:58
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(LocaleCondition.class)
public @interface Locale {

    /**
     * 系统语言
     * @return 系统语言
     */
    LocaleEnum value();
}
