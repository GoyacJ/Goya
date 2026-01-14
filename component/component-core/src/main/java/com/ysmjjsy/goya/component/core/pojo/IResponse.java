package com.ysmjjsy.goya.component.core.pojo;

import java.io.Serializable;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:32
 */
public interface IResponse extends Serializable {

    /**
     * 是否成功响应
     *
     * @return 是否成功响应
     */
    boolean isSuccess();

    /**
     * 业务状态码
     *
     * @return 业务状态码
     */
    String code();

    /**
     * 响应消息
     *
     * @return 响应消息
     */
    String message();
}
