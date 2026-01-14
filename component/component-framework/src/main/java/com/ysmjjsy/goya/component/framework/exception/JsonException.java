package com.ysmjjsy.goya.component.framework.exception;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 23:54
 */
public class JsonException extends CommonException {

    @Serial
    private static final long serialVersionUID = 8945620660729366432L;

    public JsonException() {
        super(HttpErrorCodeEnum.JSON_ERROR);
    }

    public JsonException(String message) {
        super(HttpErrorCodeEnum.JSON_ERROR);
    }

    public JsonException(Throwable cause) {
        super(HttpErrorCodeEnum.JSON_ERROR);
    }

    public JsonException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.JSON_ERROR);
    }
}
