package com.ysmjjsy.goya.component.core.exception;

import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;

/**
 * <p>exception interface</p>
 *
 * @author goya
 * @since 2026/1/7 22:55
 */
public interface IException {

    /**
     * get error code
     *
     * @return error code
     */
    ErrorCode getErrorCode();
}
