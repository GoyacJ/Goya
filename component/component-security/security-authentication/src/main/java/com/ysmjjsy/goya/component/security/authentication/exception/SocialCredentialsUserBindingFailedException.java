package com.ysmjjsy.goya.component.security.authentication.exception;

import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;

import java.io.Serial;

/**
 * <p>社交登录绑定用户出错</p>
 *
 * @author goya
 * @since 2025/12/7 23:38
 */
public class SocialCredentialsUserBindingFailedException extends SecurityAuthenticationException {
    @Serial
    private static final long serialVersionUID = -2478823641995316822L;

    public SocialCredentialsUserBindingFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SocialCredentialsUserBindingFailedException(String msg) {
        super(msg);
    }
}
