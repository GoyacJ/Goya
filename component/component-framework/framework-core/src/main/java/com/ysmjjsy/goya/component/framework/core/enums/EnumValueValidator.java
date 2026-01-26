package com.ysmjjsy.goya.component.framework.core.enums;

import com.ysmjjsy.goya.component.framework.common.enums.CodeEnum;
import com.ysmjjsy.goya.component.framework.common.enums.EnumKit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>EnumValue 校验器：校验输入值是否为指定 CodeEnum 的合法 code</p>
 *
 * @author goya
 * @since 2026/1/24 15:47
 */
public class EnumValueValidator implements ConstraintValidator<EnumValue, Object> {

    private Class<? extends Enum<?>> enumClass;

    /** {@inheritDoc} */
    @Override
    public void initialize(EnumValue annotation) {
        this.enumClass = Objects.requireNonNull(annotation.enumClass(), "enumClass 不能为空");
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (!enumClass.isEnum() || !CodeEnum.class.isAssignableFrom(enumClass)) {
            return false;
        }
        String code = String.valueOf(value);
        return EnumKit.findByCode((Class) enumClass, (Serializable) code).isPresent();
    }
}