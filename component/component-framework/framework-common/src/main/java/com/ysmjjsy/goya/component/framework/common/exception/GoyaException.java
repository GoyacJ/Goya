package com.ysmjjsy.goya.component.framework.common.exception;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <p>Goya 统一运行时异常基类，用于在系统内传递结构化错误信息。</p>
 * <p>该异常只定义“错误表达协议”，不绑定 Web/Spring/HTTP。
 * 上层（例如 {@code framework-core}）可以将该异常转换为统一响应体或错误载体。</p>
 *
 * <h2>核心字段</h2>
 * <ul>
 *   <li>{@link #errorCode()}：稳定错误码，用于治理与排障聚合。</li>
 *   <li>{@link #userMessage()}：对外安全文案（可返回给前端/调用方）。</li>
 *   <li>{@link #debugMessage()}：诊断文案（仅日志/内部使用，不建议对外）。</li>
 *   <li>{@link #args()}：用于 i18n 或消息模板格式化的参数。</li>
 *   <li>{@link #metadata()}：结构化上下文（例如 orderId、tenantId、downstream 等）。</li>
 * </ul>
 *
 * <h2>安全建议</h2>
 * <p>{@code userMessage} 不应包含敏感信息；敏感信息应记录在日志中（可脱敏）。</p>
 *
 * <h2>线程安全</h2>
 * <p>异常对象通常由单线程创建并抛出；为避免外部修改，{@code metadata} 会被封装为不可变视图。</p>
 *
 * @author goya
 * @since 2026/1/24 13:26
 */
public class GoyaException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ErrorCode errorCode;
    private final String userMessage;
    private final String debugMessage;
    private final Object[] args;
    private final Map<String, Object> metadata;

    /**
     * 构造一个仅包含错误码的异常。
     *
     * <p>该构造函数会使用错误码默认文案作为异常 message。</p>
     *
     * @param errorCode 错误码（不能为空）
     */
    public GoyaException(ErrorCode errorCode) {
        this(errorCode, null, null, null, null, null);
    }

    /**
     * 构造一个包含错误码与原因异常的异常。
     *
     * @param errorCode 错误码（不能为空）
     * @param cause 原因异常（可为空）
     */
    public GoyaException(ErrorCode errorCode, Throwable cause) {
        this(errorCode, null, null, null, null, cause);
    }

    /**
     * 全量构造函数。
     *
     * @param errorCode 错误码（不能为空）
     * @param userMessage 对外安全文案（可为空）
     * @param debugMessage 诊断文案（可为空）
     * @param args 消息参数（可为空）
     * @param metadata 结构化上下文（可为空）
     * @param cause 原因异常（可为空）
     */
    public GoyaException(
            ErrorCode errorCode,
            String userMessage,
            String debugMessage,
            Object[] args,
            Map<String, Object> metadata,
            Throwable cause
    ) {
        super(resolveThrowableMessage(errorCode, userMessage, debugMessage), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode 不能为空");
        this.userMessage = userMessage;
        this.debugMessage = debugMessage;
        this.args = args != null ? args.clone() : null;
        this.metadata = metadata != null ? Collections.unmodifiableMap(metadata) : Map.of();
    }

    /**
     * 返回错误码。
     *
     * @return 错误码（非空）
     */
    public ErrorCode errorCode() {
        return errorCode;
    }

    /**
     * 返回对外安全文案。
     *
     * @return 对外安全文案，可能为空
     */
    public String userMessage() {
        return userMessage;
    }

    /**
     * 返回诊断文案（建议只写入日志）。
     *
     * @return 诊断文案，可能为空
     */
    public String debugMessage() {
        return debugMessage;
    }

    /**
     * 返回消息参数数组（用于 i18n/模板格式化）。
     *
     * <p>为避免外部修改，返回值为拷贝。</p>
     *
     * @return 参数数组拷贝，可能为空
     */
    public Object[] args() {
        return args != null ? args.clone() : null;
    }

    /**
     * 返回结构化上下文信息。
     *
     * <p>返回的是不可变 Map 视图。</p>
     *
     * @return 结构化上下文（非空，可能为空 Map）
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * 解析 {@link Throwable#getMessage()} 的兜底策略。
     *
     * <p>优先级：debugMessage &gt; userMessage &gt; errorCode.defaultMessage()</p>
     *
     * @param errorCode 错误码
     * @param userMessage 对外文案
     * @param debugMessage 诊断文案
     * @return Throwable message
     */
    private static String resolveThrowableMessage(ErrorCode errorCode, String userMessage, String debugMessage) {
        if (debugMessage != null && !debugMessage.isBlank()) {
            return debugMessage;
        }
        if (userMessage != null && !userMessage.isBlank()) {
            return userMessage;
        }
        return errorCode.defaultMessage();
    }
}
