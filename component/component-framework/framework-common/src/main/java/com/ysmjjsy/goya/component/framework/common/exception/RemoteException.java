package com.ysmjjsy.goya.component.framework.common.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p>远程依赖异常，表示下游服务/第三方接口调用失败。</p>
 * <p>该异常是否可重试，建议由 {@link ErrorCode#retryable()} 与上层策略共同决定。</p>
 *
 * @author goya
 * @since 2026/1/24 13:28
 */
public class RemoteException extends GoyaException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造远程依赖异常。
     *
     * @param errorCode 错误码（不能为空，建议 category 为 {@code REMOTE}）
     * @param userMessage 对外安全文案（可为空）
     * @param debugMessage 诊断文案（可为空，建议只写入日志）
     * @param metadata 结构化上下文（可为空），建议包含下游标识、请求信息摘要等
     * @param cause 原因异常（可为空）
     */
    public RemoteException(ErrorCode errorCode,
                           String userMessage,
                           String debugMessage,
                           Map<String, Object> metadata,
                           Throwable cause) {
        super(errorCode, userMessage, debugMessage, null, metadata, cause);
    }
}
