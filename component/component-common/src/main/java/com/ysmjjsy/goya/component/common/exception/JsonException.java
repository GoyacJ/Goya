package com.ysmjjsy.goya.component.common.exception;

import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;

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
        super(ResponseCodeEnum.JSON_ERROR);
    }

    public JsonException(String message) {
        super(ResponseCodeEnum.JSON_ERROR);
    }

    public JsonException(Throwable cause) {
        super(ResponseCodeEnum.JSON_ERROR);
    }

    public JsonException(String message, Throwable cause) {
        super(ResponseCodeEnum.JSON_ERROR);
    }
}
