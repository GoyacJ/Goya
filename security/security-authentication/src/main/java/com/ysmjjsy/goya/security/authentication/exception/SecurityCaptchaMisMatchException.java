package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

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

    @Override
    public IResponseCode getCode() {
        return ResponseCodeEnum.CAPTCHA_MISMATCH_ERROR;
    }
}
