package com.ysmjjsy.goya.component.framework.core.api;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>字段级错误信息，用于表达参数校验失败的细节</p>
 * <p>该类型不依赖任何 Web/Spring 类型，可用于 Web、RPC、任务等多种场景的统一错误表达。</p>
 *
 * @param field 字段名（例如：name）
 * @param message 错误信息（例如：不能为空）
 * @param rejectedValue 被拒绝的值（可为空；注意脱敏后再输出到日志或响应）
 *
 * @author goya
 * @since 2026/1/24 14:51
 */
public record ApiFieldError(
        String field,
        String message,
        Object rejectedValue
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 构造字段错误（不携带 rejectedValue）。
     *
     * @param field 字段名
     * @param message 错误信息
     * @return ApiFieldError
     */
    public static ApiFieldError of(String field, String message) {
        return new ApiFieldError(field, message, null);
    }

    /**
     * 构造字段错误（携带 rejectedValue）。
     *
     * @param field 字段名
     * @param message 错误信息
     * @param rejectedValue 被拒绝值
     * @return ApiFieldError
     */
    public static ApiFieldError of(String field, String message, Object rejectedValue) {
        return new ApiFieldError(field, message, rejectedValue);
    }
}