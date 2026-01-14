package com.ysmjjsy.goya.component.core.enums;

import java.io.Serializable;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:10
 */
public interface IBizEnum<C extends Serializable>
        extends IEnum<C>, IDescribableEnum, I18nEnum {

    @Override
    default String getDescription() {
        return name();
    }
}
