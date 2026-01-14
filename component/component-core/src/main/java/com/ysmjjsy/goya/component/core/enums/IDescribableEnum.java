package com.ysmjjsy.goya.component.core.enums;

/**
 * <p>可读枚举</p>
 *
 * @author goya
 * @since 2025/12/19 23:08
 */
public interface IDescribableEnum {

    /**
     * 默认描述
     */
    default String getDescription() {
        return name();
    }

    String name();
}
