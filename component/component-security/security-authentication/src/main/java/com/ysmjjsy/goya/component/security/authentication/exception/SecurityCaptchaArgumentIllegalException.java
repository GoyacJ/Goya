package com.ysmjjsy.goya.component.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

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

    public SecurityCaptchaArgumentIllegalException(Throwable cause) {
        super("使用的验证码参数错误", cause);
    }

    public SecurityCaptchaArgumentIllegalException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public IResponseCode getCode() {
        return ResponseCodeEnum.CAPTCHA_PARAMETER_ILLEGAL_ERROR;
    }
}
