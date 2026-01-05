package com.ysmjjsy.goya.security.authentication.password;

import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

/**
 * <p>密码策略异常</p>
 * <p>当密码不符合策略要求时抛出</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public class PasswordPolicyException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = -7318259255405597893L;

    public PasswordPolicyException(String message) {
        super(message);
    }

    public PasswordPolicyException(String message, Throwable cause) {
        super(message, cause);
    }
}

