package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.BasicCodeEnum;
import com.ysmjjsy.goya.component.common.code.ICodeEnums;
import com.ysmjjsy.goya.component.common.exceptions.IExceptions;
import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/7 23:17
 */
public class SecurityAuthenticationException extends AuthenticationException implements IExceptions {

    @Serial
    private static final long serialVersionUID = -2571769474790937271L;

    public SecurityAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SecurityAuthenticationException(String msg) {
        super(msg);
    }

    @Override
    public ICodeEnums getCode() {
        return BasicCodeEnum.INTERNAL_SERVER_ERROR;
    }
}
