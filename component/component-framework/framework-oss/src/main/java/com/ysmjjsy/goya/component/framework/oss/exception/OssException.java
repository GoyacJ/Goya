package com.ysmjjsy.goya.component.framework.oss.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/19 23:44
 */
public class OssException extends GoyaException {

    @Serial
    private static final long serialVersionUID = 8945620660729366432L;

    public OssException(String userMessage) {
        super(userMessage);
    }

    public OssException(Throwable cause) {
        super(cause);
    }

    public OssException(ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage);
    }

    public OssException(ErrorCode errorCode) {
        super(errorCode);
    }

    public OssException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
