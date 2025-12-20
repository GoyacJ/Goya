package com.ysmjjsy.goya.component.common.definition.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

import java.io.Serial;

/**
 * <p>common exception</p>
 *
 * @author goya
 * @since 2025/12/19 23:36
 */
public class CommonException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = -3496208415815385901L;

    public CommonException() {
        super(ResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    public CommonException(IResponseCode code) {
        super(code);
    }

    public CommonException(String message) {
        super(message);
    }

    public CommonException(Throwable cause) {
        super(cause);
    }

    public CommonException(IResponseCode code, String message) {
        super(code, message);
    }

    public CommonException(IResponseCode code, Throwable cause) {
        super(code, cause);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommonException(IResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public CommonException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
}
