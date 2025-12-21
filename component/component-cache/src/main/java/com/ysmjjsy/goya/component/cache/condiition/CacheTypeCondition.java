package com.ysmjjsy.goya.component.cache.condiition;

import com.ysmjjsy.goya.component.cache.annotation.CacheType;
import com.ysmjjsy.goya.component.cache.enums.CacheTypeEnum;
import com.ysmjjsy.goya.component.common.condiition.AbstractEnumSpringBootCondition;

import java.lang.annotation.Annotation;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:03
 */
public class CacheTypeCondition extends AbstractEnumSpringBootCondition<CacheTypeEnum> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return CacheType.class;
    }
}
