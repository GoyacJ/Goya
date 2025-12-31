package com.ysmjjsy.goya.component.web.annotation;

import com.ysmjjsy.goya.component.web.condition.CryptoStrategyCondition;
import com.ysmjjsy.goya.component.web.enums.CryptoStrategyEnum;
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
@Conditional(CryptoStrategyCondition.class)
public @interface CryptoStrategy {

    /**
     * 加密策略
     * @return 加密策略
     */
    CryptoStrategyEnum value();
}
