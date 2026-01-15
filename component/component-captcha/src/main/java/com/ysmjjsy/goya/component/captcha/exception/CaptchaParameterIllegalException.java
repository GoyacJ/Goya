package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>验证码校验参数错误 </p>
 *
 * @author goya
 * @since 2025/12/7 23:47
 */
public class CaptchaParameterIllegalException extends CaptchaException {

    @Serial
    private static final long serialVersionUID = 811813327628213053L;

    public CaptchaParameterIllegalException() {
        super(HttpErrorCodeEnum.CAPTCHA_PARAMETER_ILLEGAL_ERROR);
    }

    public CaptchaParameterIllegalException(String message) {
        super(HttpErrorCodeEnum.CAPTCHA_PARAMETER_ILLEGAL_ERROR, message);
    }

    public CaptchaParameterIllegalException(Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_PARAMETER_ILLEGAL_ERROR, cause);
    }

    public CaptchaParameterIllegalException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_PARAMETER_ILLEGAL_ERROR, message, cause);
    }
}
