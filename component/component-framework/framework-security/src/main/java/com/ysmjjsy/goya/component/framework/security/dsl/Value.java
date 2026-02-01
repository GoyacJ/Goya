package com.ysmjjsy.goya.component.framework.security.dsl;

import java.io.Serializable;

/**
 * <p>值字面量。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface Value extends Serializable {

    /**
     * 获取值。
     *
     * @return 值
     */
    Object getValue();
}
