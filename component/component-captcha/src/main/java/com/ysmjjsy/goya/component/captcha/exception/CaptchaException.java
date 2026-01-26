package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>验证码异常</p>
 *
 * @author goya
 * @since 2025/12/9 10:22
 */
public class CaptchaException extends CommonException {

    @Serial
    private static final long serialVersionUID = -6924793489392216408L;

    public CaptchaException() {
        super(HttpErrorCodeEnum.CAPTCHA_ERROR);
    }

    public CaptchaException(HttpErrorCodeEnum code) {
        super(code);
    }

    public CaptchaException(String message) {
        super(message);
    }

    public CaptchaException(Throwable cause) {
        super(cause);
    }

    public CaptchaException(HttpErrorCodeEnum code, String message) {
        super(code, message);
    }

    public CaptchaException(HttpErrorCodeEnum code, Throwable cause) {
        super(code, cause);
    }

    public CaptchaException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaptchaException(HttpErrorCodeEnum code, String message, Throwable cause) {
        super(code, message, cause);
    }

}
