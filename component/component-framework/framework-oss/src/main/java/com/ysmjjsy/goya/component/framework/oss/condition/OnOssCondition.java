package com.ysmjjsy.goya.component.framework.oss.condition;

import com.ysmjjsy.goya.component.framework.core.condition.AbstractEnumSpringBootCondition;
import com.ysmjjsy.goya.component.framework.oss.annotation.ConditionalOnOssStrategy;
import com.ysmjjsy.goya.component.framework.oss.enums.OssEnum;

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