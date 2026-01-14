package com.ysmjjsy.goya.component.core.enums;

import java.io.Serializable;

/**
 * <p>base enum interface</p>
 *
 * @author goya
 * @since 2025/12/19 22:27
 */
public interface IEnum<C extends Serializable> {

    /**
     * code
     */
    C getCode();

    /**
     * description
     */
    String getDescription();
}
