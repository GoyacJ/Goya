package com.ysmjjsy.goya.component.social.exception;

import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

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
        super(HttpErrorCodeEnum.SOCIAL_ACCESS_ERROR);
    }

    public SocialAccessException(String message) {
        super(HttpErrorCodeEnum.SOCIAL_ACCESS_ERROR, message);
    }

    public SocialAccessException(Throwable cause) {
        super(HttpErrorCodeEnum.SOCIAL_ACCESS_ERROR, cause);
    }

    public SocialAccessException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.SOCIAL_ACCESS_ERROR, message, cause);
    }
}
