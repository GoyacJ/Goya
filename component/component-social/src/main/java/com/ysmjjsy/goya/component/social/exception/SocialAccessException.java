package com.ysmjjsy.goya.component.social.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 23:20
 */
public class SocialAccessException extends SocialException {
    @Serial
    private static final long serialVersionUID = -2765577510100722995L;


    public SocialAccessException() {
        super(ResponseCodeEnum.SOCIAL_ACCESS_ERROR);
    }

    public SocialAccessException(IResponseCode code) {
        super(code);
    }

    public SocialAccessException(String message) {
        super(message);
    }

    public SocialAccessException(Throwable cause) {
        super(cause);
    }

    public SocialAccessException(IResponseCode code, String message) {
        super(code, message);
    }

    public SocialAccessException(IResponseCode code, Throwable cause) {
        super(code, cause);
    }

    public SocialAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public SocialAccessException(IResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public SocialAccessException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
}
