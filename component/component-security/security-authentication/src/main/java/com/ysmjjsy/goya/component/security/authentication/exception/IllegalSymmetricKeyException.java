package com.ysmjjsy.goya.component.security.authentication.exception;


import com.ysmjjsy.goya.component.core.exception.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;

import java.io.Serial;

/**
 * <p>非法加密Key</p>
 *
 * @author goya
 * @since 2025/12/7 23:20
 */
public class IllegalSymmetricKeyException extends SecurityAuthenticationException {

    @Serial
    private static final long serialVersionUID = 4319230368723902966L;

    public IllegalSymmetricKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IllegalSymmetricKeyException(String msg) {
        super(msg);
    }

    @Override
    public ErrorCode getErrorCode() {
        return HttpErrorCodeEnum.CRYPTO_ERROR;
    }
}
