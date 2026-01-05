package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.security.core.exception.SecurityAuthenticationException;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/7 23:38
 */
public class UsernameAlreadyExistsException extends SecurityAuthenticationException {
    @Serial
    private static final long serialVersionUID = -1296629265981116010L;

    public UsernameAlreadyExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public UsernameAlreadyExistsException(String msg) {
        super(msg);
    }
}
