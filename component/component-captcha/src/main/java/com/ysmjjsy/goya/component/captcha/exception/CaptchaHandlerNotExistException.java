package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

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
        super(ResponseCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR);
    }

    public CaptchaHandlerNotExistException(String message) {
        super(ResponseCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR);
    }

    public CaptchaHandlerNotExistException(Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR);
    }

    public CaptchaHandlerNotExistException(String message, Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_HANDLER_NOT_EXIST_ERROR);
    }
}
