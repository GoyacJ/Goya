package com.ysmjjsy.goya.component.framework.common.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p>基础设施异常，表示数据库/IO/缓存/消息队列等基础设施层故障。</p>
 * <p>通常属于需要排查的错误，是否重试由错误码与上层策略共同决定。</p>
 *
 * @author goya
 * @since 2026/1/24 13:41
 */
public class InfraException extends GoyaException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造基础设施异常。
     *
     * @param errorCode 错误码（不能为空）
     * @param userMessage 对外安全文案（可为空）
     * @param debugMessage 诊断文案（可为空）
     * @param metadata 结构化上下文（可为空）
     * @param cause 原因异常（可为空）
     */
    public InfraException(ErrorCode errorCode,
                          String userMessage,
                          String debugMessage,
                          Map<String, Object> metadata,
                          Throwable cause) {
        super(errorCode, userMessage, debugMessage, null, metadata, cause);
    }
}