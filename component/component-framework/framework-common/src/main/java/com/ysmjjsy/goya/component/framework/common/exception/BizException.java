package com.ysmjjsy.goya.component.framework.common.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p>业务异常，表示业务规则不满足导致的失败。</p>
 * <p>业务异常通常是“预期内失败”，一般不建议重试（除非业务定义允许）。</p>
 *
 * @author goya
 * @since 2026/1/24 13:27
 */
public class BizException extends GoyaException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造业务异常。
     *
     * @param errorCode 错误码（不能为空，建议 category 为 {@code BIZ}）
     * @param userMessage 对外安全文案（可为空，为空则使用错误码默认文案）
     */
    public BizException(ErrorCode errorCode, String userMessage) {
        super(errorCode, userMessage, null, null, null, null);
    }

    /**
     * 构造业务异常（含结构化上下文）。
     *
     * @param errorCode 错误码（不能为空）
     * @param userMessage 对外安全文案（可为空）
     * @param metadata 结构化上下文（可为空）
     */
    public BizException(ErrorCode errorCode, String userMessage, Map<String, Object> metadata) {
        super(errorCode, userMessage, null, null, metadata, null);
    }
}
