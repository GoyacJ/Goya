package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

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
        super(ResponseCodeEnum.CAPTCHA_IS_EMPTY_ERROR);
    }

    public CaptchaIsEmptyException(String message) {
        super(ResponseCodeEnum.CAPTCHA_IS_EMPTY_ERROR);
    }

    public CaptchaIsEmptyException(Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_IS_EMPTY_ERROR);
    }

    public CaptchaIsEmptyException(String message, Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_IS_EMPTY_ERROR);
    }

}
