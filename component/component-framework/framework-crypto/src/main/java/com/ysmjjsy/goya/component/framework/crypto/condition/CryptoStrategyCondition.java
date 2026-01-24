package com.ysmjjsy.goya.component.framework.crypto.condition;

import com.ysmjjsy.goya.component.framework.core.condition.AbstractEnumSpringBootCondition;
import com.ysmjjsy.goya.component.framework.core.enums.CryptoStrategyEnum;

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
        return EnvCryptoStrategy.class;
    }
}
