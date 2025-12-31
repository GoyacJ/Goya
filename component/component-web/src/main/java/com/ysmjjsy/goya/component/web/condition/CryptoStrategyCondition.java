package com.ysmjjsy.goya.component.web.condition;

import com.ysmjjsy.goya.component.common.condiition.AbstractEnumSpringBootCondition;
import com.ysmjjsy.goya.component.web.annotation.CryptoStrategy;
import com.ysmjjsy.goya.component.web.enums.CryptoStrategyEnum;

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
