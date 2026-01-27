package com.ysmjjsy.goya.component.captcha.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;

import java.io.Serial;
import java.util.Map;

/**
 * <p>验证码异常</p>
 *
 * @author goya
 * @since 2025/12/9 10:22
 */
public class CaptchaException extends GoyaException {

    @Serial
    private static final long serialVersionUID = -6924793489392216408L;

    public CaptchaException(String userMessage) {
        super(userMessage);
    }

    public CaptchaException(Throwable cause) {
        super(cause);
    }

    public CaptchaException(ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage);
    }

    public CaptchaException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CaptchaException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public CaptchaException(ErrorCode errorCode, String userMessage, String debugMessage, Object[] args, Map<String, Object> metadata, Throwable cause) {
        super(errorCode, userMessage, debugMessage, args, metadata, cause);
    }
}
