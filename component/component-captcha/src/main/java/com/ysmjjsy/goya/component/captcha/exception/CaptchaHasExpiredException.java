package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>验证码已过期 </p>
 *
 * @author goya
 * @since 2025/12/7 23:47
 */
public class CaptchaHasExpiredException extends CaptchaException {

    @Serial
    private static final long serialVersionUID = 3687806079740752182L;

    public CaptchaHasExpiredException() {
        super(HttpErrorCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR);
    }

    public CaptchaHasExpiredException(String message) {
        super(HttpErrorCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR, message);
    }

    public CaptchaHasExpiredException(Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR, cause);
    }

    public CaptchaHasExpiredException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR, message, cause);
    }
}
