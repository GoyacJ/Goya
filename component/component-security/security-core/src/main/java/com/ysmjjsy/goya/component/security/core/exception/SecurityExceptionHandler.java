package com.ysmjjsy.goya.component.security.core.exception;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.core.error.ErrorMessageResolver;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.ServletErrorProperties;
import com.ysmjjsy.goya.component.framework.servlet.web.ExceptionHandlerUtils;
import com.ysmjjsy.goya.component.framework.servlet.web.HttpStatusMapper;
import com.ysmjjsy.goya.component.framework.servlet.web.ProblemDetailFactory;
import com.ysmjjsy.goya.component.framework.servlet.web.TraceIdResolver;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>安全异常处理器</p>
 * <p>专门处理 Spring Security 相关的异常，包括认证异常和授权异常。</p>
 * <p>参考 {@link com.ysmjjsy.goya.component.framework.servlet.web.GlobalExceptionHandler} 的实现方式。</p>
 *
 * <h2>处理范围</h2>
 * <ul>
 *   <li>{@link SecurityAuthenticationException}：安全认证异常（结构化错误码）</li>
 *   <li>{@link AuthenticationException}：Spring Security 认证异常（兜底处理）</li>
 *   <li>{@link AccessDeniedException}：Spring Security 授权异常</li>
 * </ul>
 *
 * <h2>安全性</h2>
 * <p>对外输出只使用"安全文案"，不会直接返回堆栈或 debugMessage。</p>
 *
 * @author goya
 * @since 2026/1/22 23:11
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    private final ErrorMessageResolver messageResolver;
    private final HttpStatusMapper statusMapper;
    private final TraceIdResolver traceIdResolver;
    private final ServletErrorProperties props;
    private final ProblemDetailFactory problemFactory;
    private final Masker masker;

    /**
     * 处理安全认证异常（结构化异常）。
     *
     * @param ex      SecurityAuthenticationException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(SecurityAuthenticationException.class)
    public ResponseEntity<?> handleSecurityAuthenticationException(SecurityAuthenticationException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        ErrorCode code = ex.errorCode();
        // 优先使用 userMessage，如果没有则通过 ErrorMessageResolver 解析
        String message = ex.userMessage();
        if (message == null || message.isBlank()) {
            message = messageResolver.resolve(code, null);
        }

        logBySeverity(ex, traceId, request);

        HttpStatus status = resolveHttpStatus(code);
        return respond(status, code, message, traceId);
    }

    /**
     * 处理 Spring Security 认证异常（兜底处理）。
     *
     * @param ex      AuthenticationException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        // 如果异常是 SecurityAuthenticationException，应该已经被上面的处理器处理了
        // 这里处理其他类型的 AuthenticationException
        ErrorCode code = CommonErrorCode.UNAUTHORIZED;
        String message = messageResolver.resolve(code, null);

        log.warn("[Security] |- Authentication failed traceId={}, path={}, message={}", 
                traceId, request.getRequestURI(), ex.getMessage());

        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return respond(status, code, message, traceId);
    }

    /**
     * 处理 Spring Security 授权异常。
     *
     * @param ex      AccessDeniedException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        ErrorCode code = CommonErrorCode.FORBIDDEN;
        String message = messageResolver.resolve(code, (Object[]) null);

        log.warn("[Security] |- Access denied traceId={}, path={}, message={}", 
                traceId, request.getRequestURI(), ex.getMessage());

        HttpStatus status = HttpStatus.FORBIDDEN;
        return respond(status, code, message, traceId);
    }

    /**
     * 根据配置输出 API 或 ProblemDetail。
     *
     * @param status  HTTP 状态码
     * @param code    错误码
     * @param message 对外文案
     * @param traceId traceId
     * @return ResponseEntity
     */
    private ResponseEntity<?> respond(HttpStatus status, ErrorCode code, String message, String traceId) {
        return ExceptionHandlerUtils.respond(status, code, message, traceId, props, problemFactory);
    }

    /**
     * AUTH 分类下对 401/403 做细分：UNAUTHORIZED=401，FORBIDDEN=403。
     *
     * @param code 错误码
     * @return HTTP 状态码
     */
    private HttpStatus resolveHttpStatus(ErrorCode code) {
        return ExceptionHandlerUtils.resolveHttpStatus(code, statusMapper);
    }

    /**
     * 按严重级别记录日志，并对 metadata 做脱敏。
     *
     * @param ex      异常
     * @param traceId traceId
     * @param request request
     */
    private void logBySeverity(SecurityAuthenticationException ex, String traceId, HttpServletRequest request) {
        ExceptionHandlerUtils.logBySeverity(
                ex.errorCode(),
                ex.userMessage(),
                ex.debugMessage(),
                ex.metadata(),
                traceId,
                request.getRequestURI(),
                masker,
                ex
        );
    }
}
