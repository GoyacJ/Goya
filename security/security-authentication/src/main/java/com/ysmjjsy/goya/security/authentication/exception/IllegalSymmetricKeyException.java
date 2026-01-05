package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;

import java.io.Serial;

/**
 * <p>非法加密Key</p>
 *
 * @author goya
 * @since 2025/12/7 23:20
 */
public class IllegalSymmetricKeyException extends com.ysmjjsy.goya.security.core.exception.SecurityAuthenticationException {

    @Serial
    private static final long serialVersionUID = 4319230368723902966L;

    public IllegalSymmetricKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IllegalSymmetricKeyException(String msg) {
        super(msg);
    }

    @Override
    public IResponseCode getCode() {
        return ResponseCodeEnum.CRYPTO_ERROR;
    }
}
