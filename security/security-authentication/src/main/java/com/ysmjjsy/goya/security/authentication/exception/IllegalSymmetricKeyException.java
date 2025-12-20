package com.ysmjjsy.goya.security.authentication.exception;

import com.ysmjjsy.goya.component.common.code.ICodeEnums;
import com.ysmjjsy.goya.component.crypto.exception.code.CryptoCodeEnum;

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
    public ICodeEnums getCode() {
        return CryptoCodeEnum.ILLEGAL_SYMMETRIC_KEY_ERROR;
    }
}
