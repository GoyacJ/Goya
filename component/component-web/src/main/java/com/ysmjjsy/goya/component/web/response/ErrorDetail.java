package com.ysmjjsy.goya.component.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ysmjjsy.goya.component.core.pojo.VO;
import com.ysmjjsy.goya.component.framework.context.SpringContext;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;

/**
 * 结构化错误信息
 * <p>
 * 用于响应体中 error 字段，便于客户端区分错误类型与定位问题。
 * <p>
 * 安全特性：
 * <ul>
 *   <li>生产环境不返回堆栈信息（防止信息泄露）</li>
 *   <li>提供多种创建方式，适应不同场景</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 带字段的错误信息（用于参数校验）
 * ErrorDetail.ofField("username", exception);
 *
 * // 带堆栈的错误信息（仅开发环境）
 * ErrorDetail.withStackTrace(exception);
 *
 * // 不带堆栈的错误信息（生产环境）
 * ErrorDetail.withoutStackTrace(exception);
 * }</pre>
 *
 * @author goya
 * @since 2025/12/20 22:09
 * @see Response
 */
@Schema(name = "ErrorDetail", description = "结构化错误详情")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDetail(
        @Schema(name = "Exception完整信息", type = "string", description = "异常消息")
        String detail,

        @Schema(name = "额外的错误字段", description = "字段校验错误时的字段名")
        String field,

        @Schema(name = "错误堆栈信息", description = "仅在开发环境返回，生产环境为 null")
        StackTraceElement[] stackTrace
) implements VO {

    /**
     * 创建带字段的错误信息
     * <p>
     * 主要用于参数校验错误，标识出错的字段名
     *
     * @param field     字段名
     * @param throwable 异常对象
     * @return ErrorDetail
     */
    public static ErrorDetail ofField(String field, Throwable throwable) {
        return new ErrorDetail(
                sanitizeMessage(throwable.getMessage()),
                field,
                getStackTrace(throwable)
        );
    }

    /**
     * 创建带堆栈信息的错误对象
     * <p>
     * 仅在开发环境返回堆栈信息，生产环境返回 null（防止信息泄露）
     *
     * @param throwable 异常对象
     * @return ErrorDetail
     */
    public static ErrorDetail withStackTrace(Throwable throwable) {
        return new ErrorDetail(
                sanitizeMessage(throwable.getMessage()),
                null,
                getStackTrace(throwable)
        );
    }

    /**
     * 创建不带堆栈信息的错误对象
     * <p>
     * 适用于生产环境，不返回堆栈信息
     *
     * @param throwable 异常对象
     * @return ErrorDetail
     */
    public static ErrorDetail withoutStackTrace(Throwable throwable) {
        return new ErrorDetail(
                sanitizeMessage(throwable.getMessage()),
                null,
                null
        );
    }

    /**
     * 创建仅包含消息的错误对象
     * <p>
     * 不包含字段和堆栈信息，适用于简单的错误场景
     *
     * @param message 错误消息
     * @return ErrorDetail
     */
    public static ErrorDetail ofMessage(String message) {
        return new ErrorDetail(
                sanitizeMessage(message),
                null,
                null
        );
    }

    /**
     * 获取堆栈信息（仅在开发环境返回）
     * <p>
     * 生产环境返回 null，防止敏感信息泄露
     *
     * @param throwable 异常对象
     * @return 堆栈信息，生产环境返回 null
     */
    private static StackTraceElement[] getStackTrace(Throwable throwable) {
        // 仅在开发环境返回堆栈信息
        if (!SpringContext.isProd()) {
            return throwable != null ? throwable.getStackTrace() : null;
        }
        return new StackTraceElement[0];
    }

    /**
     * 清理敏感信息
     * <p>
     * 移除可能包含敏感信息的异常消息
     * 可以根据需要扩展，过滤密码、token 等信息
     *
     * @param message 原始消息
     * @return 清理后的消息
     */
    private static String sanitizeMessage(String message) {
        if (StringUtils.isBlank(message)) {
            return "未知错误";
        }
         message = message.replaceAll("(?i)password[=:][^\\s]+", "password=***");
         message = message.replaceAll("(?i)token[=:][^\\s]+", "token=***");
        return message;
    }
}