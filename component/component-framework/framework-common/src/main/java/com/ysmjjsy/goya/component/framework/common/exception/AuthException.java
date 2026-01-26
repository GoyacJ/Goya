package com.ysmjjsy.goya.component.framework.common.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;

import java.util.Map;

/**
 * <p>鉴权异常，用于表达认证/授权失败语义。</p>
 *
 * @author goya
 * @since 2026/1/24 13:42
 */
public class AuthException extends GoyaException {

    /**
     * 构造鉴权异常。
     *
     * @param errorCode 错误码（不能为空）
     * @param userMessage 对外安全文案（可为空）
     * @param metadata 结构化上下文（可为空）
     */
    public AuthException(ErrorCode errorCode, String userMessage, Map<String, Object> metadata) {
        super(errorCode, userMessage, null, null, metadata, null);
    }
}
