package com.ysmjjsy.goya.component.security.authentication.exception;

import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>验证码为空</p>
 *
 * @author goya
 * @since 2025/12/7 23:35
 */
public class SecurityCaptchaIsEmptyException extends SecurityCaptchaException{

    @Serial
    private static final long serialVersionUID = -812735116112529118L;

    public SecurityCaptchaIsEmptyException(String msg) {
        super(msg);
    }

    public SecurityCaptchaIsEmptyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SecurityCaptchaIsEmptyException(Throwable cause) {
        super("验证码为空", cause);
    }

    @Override
    public ErrorCode getErrorCode() {
        return HttpErrorCodeEnum.CAPTCHA_IS_EMPTY_ERROR;
    }
}
