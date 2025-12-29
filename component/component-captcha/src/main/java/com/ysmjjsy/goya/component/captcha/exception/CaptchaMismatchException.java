package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

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
        super(ResponseCodeEnum.CAPTCHA_MISMATCH_ERROR);
    }

    public CaptchaMismatchException(String message) {
        super(ResponseCodeEnum.CAPTCHA_MISMATCH_ERROR);
    }

    public CaptchaMismatchException(Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_MISMATCH_ERROR);
    }

    public CaptchaMismatchException(String message, Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_MISMATCH_ERROR);
    }
}
