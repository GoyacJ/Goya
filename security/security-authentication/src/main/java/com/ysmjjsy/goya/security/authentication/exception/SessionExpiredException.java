package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.BasicCodeEnum;
import com.ysmjjsy.goya.component.common.code.ICodeEnums;
import com.ysmjjsy.goya.component.common.exceptions.IExceptions;
import org.springframework.security.authentication.AccountStatusException;

import java.io.Serial;

/**
 * <p>自定义 Session 已过期</p>
 *
 * @author goya
 * @since 2025/12/7 23:36
 */
public class SessionExpiredException extends AccountStatusException implements IExceptions {

    @Serial
    private static final long serialVersionUID = -366020721747280368L;

    public SessionExpiredException(String msg) {
        super(msg);
    }

    public SessionExpiredException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public ICodeEnums getCode() {
        return BasicCodeEnum.NOT_ACCEPTABLE;
    }
}
