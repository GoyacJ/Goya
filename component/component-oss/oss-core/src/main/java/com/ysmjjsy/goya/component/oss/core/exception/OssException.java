package com.ysmjjsy.goya.component.oss.core.exception;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.framework.exception.code.HttpErrorCodeEnum;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/19 23:44
 */
public class OssException extends CommonException {

    @Serial
    private static final long serialVersionUID = 8945620660729366432L;

    public OssException() {
        super(HttpErrorCodeEnum.OSS_ERROR);
    }

    public OssException(String message) {
        super(HttpErrorCodeEnum.OSS_ERROR, message);
    }

    public OssException(Throwable cause) {
        super(HttpErrorCodeEnum.OSS_ERROR, cause);
    }

    public OssException(String message, Throwable cause) {
        super(HttpErrorCodeEnum.OSS_ERROR, message, cause);
    }
}
