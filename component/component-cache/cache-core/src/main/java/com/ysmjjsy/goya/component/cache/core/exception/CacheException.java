package com.ysmjjsy.goya.component.cache.core.exception;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p>缓存异常</p>
 *
 * @author goya
 * @since 2025/12/20 23:54
 */
public class CacheException extends CommonException {

    @Serial
    private static final long serialVersionUID = 8945620660729366432L;

    public CacheException() {
        super(HttpErrorCodeEnum.CACHE_ERROR);
    }

    public CacheException(String message) {
        super(HttpErrorCodeEnum.CACHE_ERROR);
    }

    public CacheException(Throwable cause) {
        super(HttpErrorCodeEnum.CACHE_ERROR);
    }

    public CacheException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.CACHE_ERROR);
    }
}
