package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>验证码不匹配错误 </p>
 *
 * @author goya
 * @since 2025/12/7 23:47
 */
public class CaptchaMismatchException extends CaptchaException {

    @Serial
    private static final long serialVersionUID = -4536919569016320780L;

    public CaptchaMismatchException() {
        super(HttpErrorCodeEnum.CAPTCHA_MISMATCH_ERROR);
    }

    public CaptchaMismatchException(String message) {
        super(HttpErrorCodeEnum.CAPTCHA_MISMATCH_ERROR, message);
    }

    public CaptchaMismatchException(Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_MISMATCH_ERROR, cause);
    }

    public CaptchaMismatchException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_MISMATCH_ERROR, message, cause);
    }
}
