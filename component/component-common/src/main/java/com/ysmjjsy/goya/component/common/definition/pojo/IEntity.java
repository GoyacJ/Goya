package com.ysmjjsy.goya.component.common.definition.pojo;

import java.io.Serializable;

/**
 * <p>base IEntity interface</p>
 *
 * @param <T> id type
 * @author goya
 * @since 2025/12/19 22:29
 */
public interface IEntity<T extends Serializable> extends Serializable {

    /**
     * get id
     *
     * @return id
     */
    T getId();

    /**
     * set id
     *
     * @param id id
     */
    void setId(T id);
}
