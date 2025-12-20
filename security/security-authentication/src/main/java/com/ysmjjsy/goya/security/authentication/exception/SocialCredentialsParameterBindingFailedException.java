package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.BasicCodeEnum;
import com.ysmjjsy.goya.component.common.code.ICodeEnums;
import com.ysmjjsy.goya.component.common.exceptions.IExceptions;
import org.springframework.security.core.AuthenticationException;

import java.io.Serial;

/**
 * <p>无法解析SocialType错误</p>
 *
 * @author goya
 * @since 2025/12/7 23:37
 */
public class SocialCredentialsParameterBindingFailedException extends AuthenticationException implements IExceptions {
    @Serial
    private static final long serialVersionUID = 9005765762244774317L;

    public SocialCredentialsParameterBindingFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SocialCredentialsParameterBindingFailedException(String msg) {
        super(msg);
    }

    @Override
    public ICodeEnums getCode() {
        return BasicCodeEnum.NOT_ACCEPTABLE;
    }
}
