package com.ysmjjsy.goya.component.common.condiition;

import com.ysmjjsy.goya.component.common.annotation.Architecture;
import com.ysmjjsy.goya.component.common.enums.ArchitectureEnum;

import java.lang.annotation.Annotation;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:03
 */
public class ArchitectureCondition extends AbstractEnumSpringBootCondition<ArchitectureEnum> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return Architecture.class;
    }
}
