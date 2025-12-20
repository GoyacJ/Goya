package com.ysmjjsy.goya.component.common.code;

import com.ysmjjsy.goya.component.common.definition.enums.IBizEnum;
import org.springframework.http.HttpStatus;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:17
 */
public interface IResponseCode extends IBizEnum<String> {

    /**
     * 状态码
     *
     * @return 状态码
     */
    HttpStatus getStatus();

    /**
     * 是否失败
     *
     * @return 是否失败
     */
    default boolean isError() {
        return getStatus().isError();
    }

    /**
     * 是否信息
     *
     * @return 是否信息
     */
    default boolean is1xxInformational() {
        return getStatus().is1xxInformational();
    }

    /**
     * 是否成功
     *
     * @return 是否成功
     */
    default boolean is2xxSuccessful() {
        return getStatus().is2xxSuccessful();
    }

    /**
     * 是否重定向
     *
     * @return 是否重定向
     */
    default boolean is3xxRedirection() {
        return getStatus().is3xxRedirection();
    }

    /**
     * 是否客户端错误
     *
     * @return 是否客户端错误
     */
    default boolean is4xxClientError() {
        return getStatus().is4xxClientError();
    }

    /**
     * 是否服务器错误
     *
     * @return 是否服务器错误
     */
    default boolean is5xxServerError() {
        return getStatus().is5xxServerError();
    }
}
