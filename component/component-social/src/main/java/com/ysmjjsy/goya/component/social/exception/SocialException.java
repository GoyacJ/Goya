package com.ysmjjsy.goya.component.social.exception;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 23:18
 */
public class SocialException extends CommonException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SocialException() {
        this(HttpErrorCodeEnum.SOCIAL_ERROR);
    }

    public SocialException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SocialException(String message) {
        this(HttpErrorCodeEnum.SOCIAL_ERROR, message);
    }

    public SocialException(Throwable cause) {
        this(HttpErrorCodeEnum.SOCIAL_ERROR, cause);
    }

    public SocialException(String message, Throwable cause) {
        this(HttpErrorCodeEnum.SOCIAL_ERROR, message, cause);
    }

    public SocialException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public SocialException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SocialException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
