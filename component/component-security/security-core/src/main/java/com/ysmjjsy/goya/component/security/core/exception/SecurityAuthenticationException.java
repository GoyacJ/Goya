package com.ysmjjsy.goya.component.security.core.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.IException;
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
    public IResponseCode getCode() {
        return ResponseCodeEnum.INTERNAL_SERVER_ERROR;
    }
}
