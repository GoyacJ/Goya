package com.ysmjjsy.goya.component.security.authentication.exception;

import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>使用的验证码不匹配错误</p>
 *
 * @author goya
 * @since 2025/12/7 23:35
 */
public class SecurityCaptchaMisMatchException extends SecurityCaptchaException{

    @Serial
    private static final long serialVersionUID = -812735116112529118L;

    public SecurityCaptchaMisMatchException(String msg) {
        super(msg);
    }

    public SecurityCaptchaMisMatchException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SecurityCaptchaMisMatchException(Throwable cause) {
        super("使用的验证码不匹配错误", cause);
    }

    @Override
    public ErrorCode getErrorCode() {
        return HttpErrorCodeEnum.CAPTCHA_MISMATCH_ERROR;
    }
}
