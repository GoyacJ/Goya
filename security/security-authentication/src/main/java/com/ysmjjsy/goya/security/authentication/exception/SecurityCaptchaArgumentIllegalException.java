package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.captcha.exception.code.CaptchaCodeEnum;
import com.ysmjjsy.goya.component.common.code.ICodeEnums;

import java.io.Serial;

/**
 * <p>使用的验证码参数错误</p>
 *
 * @author goya
 * @since 2025/12/7 23:32
 */
public class SecurityCaptchaArgumentIllegalException extends SecurityCaptchaException {

    @Serial
    private static final long serialVersionUID = -7917355470344939298L;

    public SecurityCaptchaArgumentIllegalException(String msg) {
        super(msg);
    }

    public SecurityCaptchaArgumentIllegalException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public ICodeEnums getCode() {
        return CaptchaCodeEnum.CAPTCHA_PARAMETER_ILLEGAL_ERROR;
    }
}
