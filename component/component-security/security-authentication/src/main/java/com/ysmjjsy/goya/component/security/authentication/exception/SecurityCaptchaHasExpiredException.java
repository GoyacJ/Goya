package com.ysmjjsy.goya.component.security.authentication.exception;

import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

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
    public ErrorCode getErrorCode() {
        return HttpErrorCodeEnum.CAPTCHA_HAS_EXPIRED_ERROR;
    }
}
