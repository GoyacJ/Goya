package com.ysmjjsy.goya.component.core.exception;

import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/7 23:46
 */
public class CommonException extends AbstractRuntimeException{
    @Serial
    private static final long serialVersionUID = -1658369210895863964L;

    public CommonException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommonException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public CommonException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public CommonException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommonException(Throwable cause) {
        super(cause);
    }
}
