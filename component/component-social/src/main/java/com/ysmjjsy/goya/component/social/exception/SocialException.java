package com.ysmjjsy.goya.component.social.exception;

import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;

import java.io.Serial;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/1 23:18
 */
public class SocialException extends GoyaException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SocialException(String userMessage) {
        super(userMessage);
    }

    public SocialException(Throwable cause) {
        super(cause);
    }

    public SocialException(com.ysmjjsy.goya.component.framework.common.error.ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage);
    }

    public SocialException(com.ysmjjsy.goya.component.framework.common.error.ErrorCode errorCode) {
        super(errorCode);
    }

    public SocialException(com.ysmjjsy.goya.component.framework.common.error.ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public SocialException(com.ysmjjsy.goya.component.framework.common.error.ErrorCode errorCode, String userMessage, String debugMessage, Object[] args, Map<String, Object> metadata, Throwable cause) {
        super(errorCode, userMessage, debugMessage, args, metadata, cause);
    }
}
