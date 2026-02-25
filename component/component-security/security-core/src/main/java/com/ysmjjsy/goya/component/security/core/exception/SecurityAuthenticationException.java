package com.ysmjjsy.goya.component.security.core.exception;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import org.springframework.security.core.AuthenticationException;

import java.io.Serial;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * <p>安全认证异常</p>
 * <p>继承 Spring Security 的 {@link AuthenticationException}，同时提供类似 {@link com.ysmjjsy.goya.component.framework.common.exception.GoyaException} 的结构化错误信息。</p>
 * <p>用于在认证流程中传递结构化错误码和用户友好的错误消息。</p>
 *
 * <h2>核心字段</h2>
 * <ul>
 *   <li>{@link #errorCode}：稳定错误码，用于治理与排障聚合。</li>
 *   <li>{@link #userMessage}：对外安全文案（可返回给前端/调用方）。</li>
 *   <li>{@link #debugMessage}：诊断文案（仅日志/内部使用，不建议对外）。</li>
 *   <li>{@link #metadata}：结构化上下文（例如 userId、tenantId、loginType 等）。</li>
 * </ul>
 *
 * <h2>安全建议</h2>
 * <p>{@code userMessage} 不应包含敏感信息；敏感信息应记录在日志中（可脱敏）。</p>
 *
 * @author goya
 * @since 2025/12/7 23:17
 */
public class SecurityAuthenticationException extends AuthenticationException {

    @Serial
    private static final long serialVersionUID = -2571769474790937271L;

    /**
     * 错误码（不能为空）
     */
    private final ErrorCode errorCode;

    /**
     * 对外安全文案（可为空）
     */
    private final String userMessage;

    /**
     * 诊断文案（可为空）
     */
    private final String debugMessage;

    /**
     * 结构化上下文（可为空）
     */
    private final Map<String, Object> metadata;

    /**
     * 构造一个仅包含错误码的异常。
     * <p>该构造函数会使用错误码默认文案作为异常 message。</p>
     *
     * @param errorCode 错误码（不能为空）
     */
    public SecurityAuthenticationException(ErrorCode errorCode) {
        this(errorCode, null, null, null, null);
    }

    /**
     * 构造一个包含错误码和用户消息的异常。
     *
     * @param errorCode   错误码（不能为空）
     * @param userMessage 对外安全文案（可为空）
     */
    public SecurityAuthenticationException(ErrorCode errorCode, String userMessage) {
        this(errorCode, userMessage, null, null, null);
    }

    /**
     * 构造一个包含错误码、用户消息和原因异常的异常。
     *
     * @param errorCode   错误码（不能为空）
     * @param userMessage 对外安全文案（可为空）
     * @param cause       原因异常（可为空）
     */
    public SecurityAuthenticationException(ErrorCode errorCode, String userMessage, Throwable cause) {
        this(errorCode, userMessage, null, null, cause);
    }

    /**
     * 构造一个包含错误码、用户消息、诊断消息和原因异常的异常。
     *
     * @param errorCode    错误码（不能为空）
     * @param userMessage  对外安全文案（可为空）
     * @param debugMessage 诊断文案（可为空）
     * @param cause        原因异常（可为空）
     */
    public SecurityAuthenticationException(ErrorCode errorCode, String userMessage, String debugMessage, Throwable cause) {
        this(errorCode, userMessage, debugMessage, null, cause);
    }

    /**
     * 全量构造函数。
     *
     * @param errorCode    错误码（不能为空）
     * @param userMessage  对外安全文案（可为空）
     * @param debugMessage 诊断文案（可为空）
     * @param metadata     结构化上下文（可为空）
     * @param cause        原因异常（可为空）
     */
    public SecurityAuthenticationException(ErrorCode errorCode,
                                          String userMessage,
                                          String debugMessage,
                                          Map<String, Object> metadata,
                                          Throwable cause) {
        super(resolveThrowableMessage(errorCode, userMessage, debugMessage), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode 不能为空");
        this.userMessage = userMessage;
        this.debugMessage = debugMessage;
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
     * 返回结构化上下文信息。
     * <p>返回的是不可变 Map 视图。</p>
     *
     * @return 结构化上下文（非空，可能为空 Map）
     */
    public Map<String, Object> metadata() {
        return metadata;
    }

    /**
     * 解析 {@link Throwable#getMessage()} 的兜底策略。
     * <p>优先级：debugMessage &gt; userMessage &gt; errorCode.defaultMessage()</p>
     *
     * @param errorCode    错误码
     * @param userMessage  对外文案
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

    // ========== 便捷构造方法 ==========

    /**
     * 创建一个未认证异常。
     *
     * @param userMessage 用户消息
     * @return SecurityAuthenticationException
     */
    public static SecurityAuthenticationException unauthorized(String userMessage) {
        return new SecurityAuthenticationException(CommonErrorCode.UNAUTHORIZED, userMessage);
    }

    /**
     * 创建一个未认证异常（带原因）。
     *
     * @param userMessage 用户消息
     * @param cause       原因异常
     * @return SecurityAuthenticationException
     */
    public static SecurityAuthenticationException unauthorized(String userMessage, Throwable cause) {
        return new SecurityAuthenticationException(CommonErrorCode.UNAUTHORIZED, userMessage, cause);
    }

    /**
     * 创建一个认证失败异常。
     *
     * @param userMessage 用户消息
     * @return SecurityAuthenticationException
     */
    public static SecurityAuthenticationException authenticationFailed(String userMessage) {
        return new SecurityAuthenticationException(CommonErrorCode.UNAUTHORIZED, userMessage);
    }

    /**
     * 创建一个认证失败异常（带原因）。
     *
     * @param userMessage 用户消息
     * @param cause       原因异常
     * @return SecurityAuthenticationException
     */
    public static SecurityAuthenticationException authenticationFailed(String userMessage, Throwable cause) {
        return new SecurityAuthenticationException(CommonErrorCode.UNAUTHORIZED, userMessage, cause);
    }
}
