package com.ysmjjsy.goya.component.framework.annotation;

import com.ysmjjsy.goya.component.framework.condiition.CryptoStrategyCondition;
import com.ysmjjsy.goya.component.framework.enums.CryptoStrategyEnum;
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
public @interface EnvCryptoStrategy {

    /**
     * 加密策略
     * @return 加密策略
     */
    CryptoStrategyEnum value();
}
