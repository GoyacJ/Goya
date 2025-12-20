package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.captcha.exception.code.CaptchaCodeEnum;
import com.ysmjjsy.goya.component.common.code.ICodeEnums;

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
    public ICodeEnums getCode() {
        return CaptchaCodeEnum.CAPTCHA_IS_EMPTY_ERROR;
    }
}
