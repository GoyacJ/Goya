package com.ysmjjsy.goya.component.framework.common.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;

import java.io.Serial;
import java.util.Map;

/**
 * <p>参数/校验异常，表示输入不合法或不满足约束条件。</p>
 * <p>该异常通常属于客户端问题，不建议重试。</p>
 *
 * @author goya
 * @since 2026/1/24 13:27
 */
public class ValidationException extends GoyaException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造校验异常。
     *
     * @param errorCode 错误码（不能为空，建议 category 为 {@code VALIDATION}）
     * @param userMessage 对外安全文案（可为空）
     * @param args 消息参数（可为空），用于 i18n/模板格式化
     * @param metadata 结构化上下文（可为空）
     */
    public ValidationException(ErrorCode errorCode, String userMessage, Object[] args, Map<String, Object> metadata) {
        super(errorCode, userMessage, null, args, metadata, null);
    }
}
