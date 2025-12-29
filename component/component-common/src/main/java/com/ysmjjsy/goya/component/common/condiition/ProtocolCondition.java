package com.ysmjjsy.goya.component.common.condiition;

import com.ysmjjsy.goya.component.common.annotation.Protocol;
import com.ysmjjsy.goya.component.common.enums.ProtocolEnum;

import java.lang.annotation.Annotation;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:03
 */
public class ProtocolCondition extends AbstractEnumSpringBootCondition<ProtocolEnum> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return Protocol.class;
    }
}
