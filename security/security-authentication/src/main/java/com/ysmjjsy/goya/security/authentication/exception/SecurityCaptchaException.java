package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.IException;
import org.springframework.security.authentication.AccountStatusException;

import java.io.Serial;

/**
 * <p>Description: OAuth2 验证码基础 Exception </p>
 * <p>
 * 这里没有用基础定义的 SecurityAuthenticationException。主要问题是在自定义表单登录时，如果使用基础的 {@link org.springframework.security.core.AuthenticationException}，
 * 在 Spring Security 标准代码中该Exception将不会抛出，而是进行二次的用户验证，这将导致在验证过程中直接跳过验证码的校验。
 *
 * @author goya
 * @since 2025/12/7 23:30
 */
public class SecurityCaptchaException extends AccountStatusException implements IException {

    @Serial
    private static final long serialVersionUID = 231126901614980301L;

    public SecurityCaptchaException(String msg) {
        super(msg);
    }

    public SecurityCaptchaException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public IResponseCode getCode() {
        return ResponseCodeEnum.INTERNAL_SERVER_ERROR;
    }
}
