package com.ysmjjsy.goya.component.oss.core.condition;

import com.ysmjjsy.goya.component.framework.condiition.AbstractEnumSpringBootCondition;
import com.ysmjjsy.goya.component.oss.core.annotation.ConditionalOnOssStrategy;
import com.ysmjjsy.goya.component.oss.core.enums.OssEnum;

import java.lang.annotation.Annotation;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/3 09:25
 */
public class OnOssCondition extends AbstractEnumSpringBootCondition<OssEnum> {

    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return ConditionalOnOssStrategy.class;
    }
}