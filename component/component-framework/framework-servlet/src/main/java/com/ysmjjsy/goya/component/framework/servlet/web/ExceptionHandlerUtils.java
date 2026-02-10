package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.Severity;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.ServletErrorProperties;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * <p>异常处理工具类</p>
 * <p>提供异常处理的公共方法，供多个异常处理器共享使用。</p>
 *
 * @author goya
 * @since 2026/2/4
 */
@Slf4j
@UtilityClass
public class ExceptionHandlerUtils {

    /**
     * 根据配置输出 API 或 ProblemDetail。
     *
     * @param status        HTTP 状态码
     * @param code          错误码
     * @param message       对外文案
     * @param traceId       traceId
     * @param props         配置项
     * @param problemFactory ProblemDetail 工厂
     * @return ResponseEntity
     */
    public ResponseEntity<?> respond(HttpStatus status,
                                     ErrorCode code,
                                     String message,
                                     String traceId,
                                     ServletErrorProperties props,
                                     ProblemDetailFactory problemFactory) {
        return switch (props.responseStyle()) {
            case API -> ResponseEntity.status(status).body(ApiRes.fail(code, message, traceId));
            case PROBLEM, BOTH -> ResponseEntity.status(status).body(problemFactory.fromCode(code, message, traceId));
        };
    }

    /**
     * AUTH 分类下对 401/403 做细分：UNAUTHORIZED=401，FORBIDDEN=403。
     *
     * @param code        错误码
     * @param statusMapper 状态码映射器
     * @return HTTP 状态码
     */
    public HttpStatus resolveHttpStatus(ErrorCode code, HttpStatusMapper statusMapper) {
        if (code == CommonErrorCode.UNAUTHORIZED) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code == CommonErrorCode.FORBIDDEN) {
            return HttpStatus.FORBIDDEN;
        }
        return statusMapper.map(code.category());
    }

    /**
     * 按严重级别记录日志，并对 metadata 做脱敏。
     *
     * @param errorCode   错误码
     * @param userMessage 用户消息
     * @param debugMessage 调试消息
     * @param metadata    元数据
     * @param traceId     traceId
     * @param path        请求路径
     * @param masker      脱敏器
     * @param throwable   异常（可选，用于 ERROR 级别日志）
     */
    public void logBySeverity(ErrorCode errorCode,
                              String userMessage,
                              String debugMessage,
                              Map<String, Object> metadata,
                              String traceId,
                              String path,
                              Masker masker,
                              Throwable throwable) {
        Severity sev = errorCode.severity();
        Object maskedMeta = masker.mask(metadata);

        switch (sev) {
            case INFO -> log.info("业务异常(INFO) traceId={}, uri={}, code={}, category={}, userMessage={}, metadata={}",
                    traceId, path, errorCode.code(), errorCode.category(), userMessage, maskedMeta);
            case WARN -> log.warn("业务异常(WARN) traceId={}, uri={}, code={}, category={}, userMessage={}, debugMessage={}, metadata={}",
                    traceId, path, errorCode.code(), errorCode.category(), userMessage, debugMessage, maskedMeta);
            case ERROR -> log.error("业务异常(ERROR) traceId={}, uri={}, code={}, category={}, userMessage={}, debugMessage={}, metadata={}",
                    traceId, path, errorCode.code(), errorCode.category(), userMessage, debugMessage, maskedMeta, throwable);
            case FATAL -> log.error("业务异常(FATAL) traceId={}, uri={}, code={}, category={}, userMessage={}, debugMessage={}, metadata={}",
                    traceId, path, errorCode.code(), errorCode.category(), userMessage, debugMessage, maskedMeta, throwable);
            default -> {
                if (throwable != null) {
                    log.error("Unexpected severity: {} traceId={}, uri={}, code={}, category={}, userMessage={}, metadata={}",
                            sev, traceId, path, errorCode.code(), errorCode.category(), userMessage, maskedMeta, throwable);
                } else {
                    log.error("Unexpected severity: {} traceId={}, uri={}, code={}, category={}, userMessage={}, metadata={}",
                            sev, traceId, path, errorCode.code(), errorCode.category(), userMessage, maskedMeta);
                }
            }
        }
    }

    /**
     * 按严重级别记录日志（简化版本，不包含 debugMessage）。
     *
     * @param errorCode   错误码
     * @param userMessage 用户消息
     * @param metadata    元数据
     * @param traceId     traceId
     * @param path        请求路径
     * @param masker      脱敏器
     * @param throwable   异常（可选，用于 ERROR 级别日志）
     */
    public void logBySeverity(ErrorCode errorCode,
                              String userMessage,
                              Map<String, Object> metadata,
                              String traceId,
                              String path,
                              Masker masker,
                              Throwable throwable) {
        logBySeverity(errorCode, userMessage, null, metadata, traceId, path, masker, throwable);
    }
}
