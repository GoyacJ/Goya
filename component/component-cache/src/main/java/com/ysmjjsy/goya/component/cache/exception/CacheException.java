package com.ysmjjsy.goya.component.cache.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.AbstractRuntimeException;

import java.io.Serial;

/**
 * <p>缓存异常</p>
 *
 * @author goya
 * @since 2025/12/21 22:52
 */
public class CacheException extends AbstractRuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CacheException() {
        super(ResponseCodeEnum.CACHE_ERROR);
    }

    public CacheException(IResponseCode code) {
        super(code);
    }

    public CacheException(String message) {
        super(message);
    }

    public CacheException(Throwable cause) {
        super(cause);
    }

    public CacheException(IResponseCode code, String message) {
        super(code, message);
    }

    public CacheException(IResponseCode code, Throwable cause) {
        super(code, cause);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheException(IResponseCode code, String message, Throwable cause) {
        super(code, message, cause);
    }

    public CacheException(IResponseCode code, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(code, message, cause, enableSuppression, writableStackTrace);
    }
}
