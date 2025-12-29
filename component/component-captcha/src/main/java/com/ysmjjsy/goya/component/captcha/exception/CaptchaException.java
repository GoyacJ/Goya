package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.AbstractRuntimeException;

import java.io.Serial;

/**
 * <p>验证码异常</p>
 *
 * @author goya
 * @since 2025/12/9 10:22
 */
public class CaptchaException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = -6924793489392216408L;

    public CaptchaException() {
        super(ResponseCodeEnum.CAPTCHA_ERROR);
    }

    public CaptchaException(IResponseCode code) {
        super(code);
    }

    public CaptchaException(String message) {
        super(message);
    }

    public CaptchaException(Throwable cause) {
        super(cause);
    }

    public CaptchaException(IResponseCode code, String message) {
        super(code, message);
    }

    public CaptchaException(IResponseCode code, Throwable cause) {
        super(code, cause);
    }

    public CaptchaException(String message, Throwable cause) {
        super(message, cause);
    }

    public CaptchaException(IResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public CaptchaException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }

}
