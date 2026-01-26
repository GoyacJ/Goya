package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>验证码为空 </p>
 *
 * @author goya
 * @since 2025/12/7 23:47
 */
public class CaptchaIsEmptyException extends CaptchaException {

    @Serial
    private static final long serialVersionUID = 328882824252590817L;

    public CaptchaIsEmptyException() {
        super(HttpErrorCodeEnum.CAPTCHA_IS_EMPTY_ERROR);
    }

    public CaptchaIsEmptyException(String message) {
        super(HttpErrorCodeEnum.CAPTCHA_IS_EMPTY_ERROR, message);
    }

    public CaptchaIsEmptyException(Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_IS_EMPTY_ERROR, cause);
    }

    public CaptchaIsEmptyException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_IS_EMPTY_ERROR, message, cause);
    }

}
