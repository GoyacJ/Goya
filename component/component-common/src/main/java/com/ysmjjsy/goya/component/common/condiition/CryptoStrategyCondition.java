package com.ysmjjsy.goya.component.common.condiition;

import com.ysmjjsy.goya.component.common.annotation.CryptoStrategy;
import com.ysmjjsy.goya.component.common.enums.CryptoStrategyEnum;

import java.lang.annotation.Annotation;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:03
 */
public class CryptoStrategyCondition extends AbstractEnumSpringBootCondition<CryptoStrategyEnum> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return CryptoStrategy.class;
    }
}
