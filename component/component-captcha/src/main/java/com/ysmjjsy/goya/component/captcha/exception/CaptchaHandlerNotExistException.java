package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>验证码处理器不存在</p>
 *
 * @author goya
 * @since 2025/12/7 23:47
 */
public class CaptchaHandlerNotExistException extends CaptchaException {

    @Serial
    private static final long serialVersionUID = -1674108136062398202L;

    public CaptchaHandlerNotExistException() {
        super(HttpErrorCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR);
    }

    public CaptchaHandlerNotExistException(String message) {
        super(HttpErrorCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR, message);
    }

    public CaptchaHandlerNotExistException(Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR, cause);
    }

    public CaptchaHandlerNotExistException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR, message, cause);
    }
}
