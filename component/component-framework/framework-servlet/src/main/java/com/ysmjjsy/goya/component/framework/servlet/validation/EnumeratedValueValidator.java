package com.ysmjjsy.goya.component.framework.servlet.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * <p>枚举值校验逻辑</p>
 *
 * @author goya
 * @since 2025/10/9 09:42
 */
public class EnumeratedValueValidator implements ConstraintValidator<EnumeratedValue, Object> {

    private String[] names;
    private int[] ordinals;

    @Override
    public void initialize(EnumeratedValue constraintAnnotation) {
        names = constraintAnnotation.names();
        ordinals = constraintAnnotation.ordinals();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value instanceof String) {
            for (String name : names) {
                if (name.equals(value)) {
                    return true;
                }
            }
        } else if (value instanceof Integer intV) {
            for (int ordinal : ordinals) {
                if (ordinal == intV) {
                    return true;
                }
            }
        }
        return false;
    }
}
