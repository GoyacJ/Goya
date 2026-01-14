package com.ysmjjsy.goya.component.core.exception;

import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import lombok.Getter;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/7 23:12
 */
public class AbstractSystemException extends Exception implements IException {

    @Serial
    private static final long serialVersionUID = -6620047766708434410L;

    @Getter
    private final ErrorCode errorCode;

    protected AbstractSystemException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    protected AbstractSystemException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    protected AbstractSystemException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, cause);
    }

    protected AbstractSystemException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    protected AbstractSystemException(String message) {
        super(message);
        this.errorCode = null;
    }

    protected AbstractSystemException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    protected AbstractSystemException(Throwable cause) {
        super(cause);
        this.errorCode = null;
    }
}
