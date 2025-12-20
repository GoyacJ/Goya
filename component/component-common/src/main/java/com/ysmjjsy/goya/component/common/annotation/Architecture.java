package com.ysmjjsy.goya.component.common.annotation;

import com.ysmjjsy.goya.component.common.condiition.ArchitectureCondition;
import com.ysmjjsy.goya.component.common.enums.ArchitectureEnum;
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
@Conditional(ArchitectureCondition.class)
public @interface Architecture {

    /**
     * 系统架构模式
     * @return 系统架构模式
     */
    ArchitectureEnum value();
}
