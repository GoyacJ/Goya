package com.ysmjjsy.goya.component.common.definition.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

/**
 * <p>abstract runtime exception</p>
 *
 * @author goya
 * @since 2025/12/19 23:16
 */
public class AbstractRuntimeException extends RuntimeException implements IException {
    @Serial
    private static final long serialVersionUID = -4736030455914143575L;

    @Getter
    private final IResponseCode code;

    protected AbstractRuntimeException() {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    protected AbstractRuntimeException(IResponseCode code) {
        this(code, StringUtils.EMPTY);
    }

    protected AbstractRuntimeException(String message) {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR, message);
    }

    protected AbstractRuntimeException(Throwable cause) {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR, cause);
    }

    protected AbstractRuntimeException(IResponseCode code, String message) {
        this(code, message, null);
    }

    protected AbstractRuntimeException(IResponseCode code, Throwable cause) {
        this(code, StringUtils.EMPTY, cause);
    }

    protected AbstractRuntimeException(String message, Throwable cause) {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR, message, cause);
    }

    protected AbstractRuntimeException(IResponseCode code, String message, Throwable cause) {
        super(IException.formatMessage(code, message), cause);
        this.code = code;
    }

    protected AbstractRuntimeException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(IException.formatMessage(code, message), cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
