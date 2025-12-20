package com.ysmjjsy.goya.component.common.exception;

import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 22:13
 */
public class ParamsValidationException extends CommonException {

    @Serial
    private static final long serialVersionUID = 8945620660729366432L;

    public ParamsValidationException() {
        super(ResponseCodeEnum.PARAMS_VALIDATION_ERROR);
    }

    public ParamsValidationException(String message) {
        super(ResponseCodeEnum.PARAMS_VALIDATION_ERROR);
    }

    public ParamsValidationException(Throwable cause) {
        super(ResponseCodeEnum.PARAMS_VALIDATION_ERROR);
    }

    public ParamsValidationException(String message, Throwable cause) {
        super(ResponseCodeEnum.PARAMS_VALIDATION_ERROR);
    }
}
