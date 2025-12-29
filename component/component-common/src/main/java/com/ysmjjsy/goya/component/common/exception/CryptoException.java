package com.ysmjjsy.goya.component.common.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 17:37
 */
public class CryptoException extends CommonException {

    @Serial
    private static final long serialVersionUID = -6045154783273406031L;

    public CryptoException() {
        super(ResponseCodeEnum.CRYPTO_ERROR);
    }

    public CryptoException(IResponseCode code) {
        super(code);
    }

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }

    public CryptoException(IResponseCode code, String message) {
        super(code, message);
    }

    public CryptoException(IResponseCode code, Throwable cause) {
        super(code, cause);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(IResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public CryptoException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
}
