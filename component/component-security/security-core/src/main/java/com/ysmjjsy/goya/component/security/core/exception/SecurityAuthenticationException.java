package com.ysmjjsy.goya.component.security.core.exception;

import com.ysmjjsy.goya.component.core.exception.IException;
import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;
import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/7 23:17
 */
public class SecurityAuthenticationException extends AuthenticationException implements IException {

    @Serial
    private static final long serialVersionUID = -2571769474790937271L;

    public SecurityAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SecurityAuthenticationException(String msg) {
        super(msg);
    }

    @Override
    public ErrorCode getErrorCode() {
        return HttpErrorCodeEnum.INTERNAL_SERVER_ERROR;
    }
}
