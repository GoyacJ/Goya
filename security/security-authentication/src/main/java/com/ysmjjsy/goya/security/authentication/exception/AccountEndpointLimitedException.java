package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.IException;
import org.springframework.security.authentication.AccountStatusException;

import java.io.Serial;

/**
 * <p>登录端点限制</p>
 *
 * @author goya
 * @since 2025/12/7 23:20
 */
public class AccountEndpointLimitedException extends AccountStatusException implements IException {

    @Serial
    private static final long serialVersionUID = 7643259479216580713L;


    public AccountEndpointLimitedException(String msg) {
        super(msg);
    }

    public AccountEndpointLimitedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    @Override
    public IResponseCode getCode() {
        return ResponseCodeEnum.NOT_ACCEPTABLE;
    }
}
