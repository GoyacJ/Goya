package com.ysmjjsy.goya.component.core.exception;

import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import lombok.Getter;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/7 22:55
 */
public abstract class AbstractRuntimeException extends RuntimeException implements IException {

    @Serial
    private static final long serialVersionUID = -855132903550300203L;

    @Getter
    private final ErrorCode errorCode;

    protected AbstractRuntimeException() {
        super();
        this.errorCode = null;
    }

    protected AbstractRuntimeException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    protected AbstractRuntimeException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    protected AbstractRuntimeException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, cause);
    }

    protected AbstractRuntimeException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    protected AbstractRuntimeException(String message) {
        super(message);
        this.errorCode = null;
    }

    protected AbstractRuntimeException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    protected AbstractRuntimeException(Throwable cause) {
        super(cause);
        this.errorCode = null;
    }

}
