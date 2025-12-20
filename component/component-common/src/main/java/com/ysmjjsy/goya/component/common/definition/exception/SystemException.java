package com.ysmjjsy.goya.component.common.definition.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

/**
 * <p>system exception</p>
 *
 * @author goya
 * @since 2025/12/19 23:35
 */
public class SystemException extends Exception implements IException {

    @Serial
    private static final long serialVersionUID = 3071133255031288341L;

    @Getter
    private final IResponseCode code;

    protected SystemException() {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    protected SystemException(IResponseCode code) {
        this(code, StringUtils.EMPTY);
    }

    protected SystemException(String message) {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR, message);
    }

    protected SystemException(Throwable cause) {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR, cause);
    }

    protected SystemException(IResponseCode code, String message) {
        this(code, message, null);
    }

    protected SystemException(IResponseCode code, Throwable cause) {
        this(code, StringUtils.EMPTY, cause);
    }

    protected SystemException(String message, Throwable cause) {
        this(ResponseCodeEnum.INTERNAL_SERVER_ERROR, message, cause);
    }

    protected SystemException(IResponseCode code, String message, Throwable cause) {
        super(IException.formatMessage(code, message), cause);
        this.code = code;
    }

    protected SystemException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(IException.formatMessage(code, message), cause, enableSuppression, writableStackTrace);
        this.code = code;
    }
}
