package com.ysmjjsy.goya.component.bus.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.AbstractRuntimeException;

import java.io.Serial;

/**
 * <p>缓存异常</p>
 *
 * @author goya
 * @since 2025/12/21 22:52
 */
public class BusException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BusException() {
        super(ResponseCodeEnum.BUS_ERROR);
    }

    public BusException(IResponseCode code) {
        super(code);
    }

    public BusException(String message) {
        super(message);
    }

    public BusException(Throwable cause) {
        super(cause);
    }

    public BusException(IResponseCode code, String message) {
        super(code, message);
    }

    public BusException(IResponseCode code, Throwable cause) {
        super(code, cause);
    }

    public BusException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusException(IResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public BusException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
}
