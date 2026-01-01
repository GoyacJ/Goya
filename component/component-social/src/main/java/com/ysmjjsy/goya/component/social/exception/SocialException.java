package com.ysmjjsy.goya.component.social.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.AbstractRuntimeException;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 23:18
 */
public class SocialException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SocialException() {
        super(ResponseCodeEnum.SOCIAL_ERROR);
    }

    public SocialException(IResponseCode code) {
        super(code);
    }

    public SocialException(String message) {
        super(message);
    }

    public SocialException(Throwable cause) {
        super(cause);
    }

    public SocialException(IResponseCode code, String message) {
        super(code, message);
    }

    public SocialException(IResponseCode code, Throwable cause) {
        super(code, cause);
    }

    public SocialException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocialException(IResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public SocialException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
}
