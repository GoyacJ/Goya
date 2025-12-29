package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

import java.io.Serial;

/**
 * <p>验证码已过期 </p>
 *
 * @author goya
 * @since 2025/12/7 23:47
 */
public class CaptchaHasExpiredException extends CaptchaException {

    @Serial
    private static final long serialVersionUID = 3687806079740752182L;

    public CaptchaHasExpiredException() {
        super(ResponseCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR);
    }

    public CaptchaHasExpiredException(String message) {
        super(ResponseCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR);
    }

    public CaptchaHasExpiredException(Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR);
    }

    public CaptchaHasExpiredException(String message, Throwable cause) {
        super(ResponseCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR);
    }
}
