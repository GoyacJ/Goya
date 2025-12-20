package com.ysmjjsy.goya.component.common.condiition;

import com.ysmjjsy.goya.component.common.annotation.Locale;
import com.ysmjjsy.goya.component.common.enums.LocaleEnum;

import java.lang.annotation.Annotation;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:03
 */
public class LocaleCondition extends AbstractEnumSpringBootCondition<LocaleEnum> {
    @Override
    protected Class<? extends Annotation> getAnnotationClass() {
        return Locale.class;
    }
}
