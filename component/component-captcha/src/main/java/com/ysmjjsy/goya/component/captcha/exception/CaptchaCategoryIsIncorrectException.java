package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

import java.io.Serial;

/**
 * <p>验证码分类错误</p>
 *
 * @author goya
 * @since 2025/12/7 23:47
 */
public class CaptchaCategoryIsIncorrectException extends CaptchaException {

    @Serial
    private static final long serialVersionUID = 4416974601189030617L;

    public CaptchaCategoryIsIncorrectException() {
        super(ResponseCodeEnum.CAPTCHA_CATEGORY_IS_INCORRECT_ERROR);
    }

    public CaptchaCategoryIsIncorrectException(String message) {
        super(ResponseCodeEnum.CAPTCHA_CATEGORY_IS_INCORRECT_ERROR);
    }

    public CaptchaCategoryIsIncorrectException(Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_CATEGORY_IS_INCORRECT_ERROR);
    }

    public CaptchaCategoryIsIncorrectException(String message, Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_CATEGORY_IS_INCORRECT_ERROR);
    }
}
