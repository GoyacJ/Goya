package com.ysmjjsy.goya.component.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

import java.io.Serial;

/**
 * <p>验证码已过期</p>
 *
 * @author goya
 * @since 2025/12/7 23:33
 */
public class SecurityCaptchaHasExpiredException extends SecurityCaptchaException {
    @Serial
    private static final long serialVersionUID = -812735116112529118L;

    public SecurityCaptchaHasExpiredException(String msg) {
        super(msg);
    }

    public SecurityCaptchaHasExpiredException(Throwable cause) {
        super("验证码已过期", cause);
    }

    public SecurityCaptchaHasExpiredException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public IResponseCode getCode() {
        return ResponseCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR;
    }
}
