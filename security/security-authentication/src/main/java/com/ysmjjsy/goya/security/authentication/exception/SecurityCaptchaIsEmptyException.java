package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

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

    @Override
    public IResponseCode getCode() {
        return ResponseCodeEnum.CAPTCHA_IS_EMPTY_ERROR;
    }
}
