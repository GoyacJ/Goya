package com.ysmjjsy.goya.component.cache.exception;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>缓存异常</p>
 *
 * @author goya
 * @since 2025/12/21 22:52
 */
public class CacheException extends CommonException {

    @Serial
    private static final long serialVersionUID = 1L;

    public CacheException() {
        super(HttpErrorCodeEnum.CACHE_ERROR);
    }

    public CacheException(HttpErrorCodeEnum code) {
        super(code);
    }

    public CacheException(String message) {
        super(message);
    }

    public CacheException(Throwable cause) {
        super(cause);
    }

    public CacheException(HttpErrorCodeEnum code, String message) {
        super(code, message);
    }

    public CacheException(HttpErrorCodeEnum code, Throwable cause) {
        super(code, cause);
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheException(HttpErrorCodeEnum code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
