package com.ysmjjsy.goya.component.cache.annotation;

import com.ysmjjsy.goya.component.cache.condiition.CacheTypeCondition;
import com.ysmjjsy.goya.component.cache.enums.CacheTypeEnum;
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
@Conditional(CacheTypeCondition.class)
public @interface CacheType {

    /**
     * 缓存类型枚举
     * @return 缓存类型枚举
     */
    CacheTypeEnum value();
}
