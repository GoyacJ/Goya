package com.ysmjjsy.goya.component.bus.core.exception;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>事件总线异常</p>
 *
 * @author goya
 * @since 2026/1/15 16:07
 */
public class BusException extends CommonException {

    @Serial
    private static final long serialVersionUID = 8945620660729366432L;

    public BusException() {
        super(HttpErrorCodeEnum.BUS_ERROR);
    }

    public BusException(String message) {
        super(HttpErrorCodeEnum.BUS_ERROR, message);
    }

    public BusException(Throwable cause) {
        super(HttpErrorCodeEnum.BUS_ERROR, cause);
    }

    public BusException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.BUS_ERROR, message, cause);
    }
}
