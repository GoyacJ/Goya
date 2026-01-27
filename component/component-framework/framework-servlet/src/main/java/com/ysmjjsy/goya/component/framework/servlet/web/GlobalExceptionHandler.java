package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.Severity;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.core.api.ApiFieldError;
import com.ysmjjsy.goya.component.framework.core.api.ApiResponse;
import com.ysmjjsy.goya.component.framework.core.error.ErrorMessageResolver;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.ServletErrorProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Servlet Web 全局异常处理器</p>
 * <p>该处理器将系统内抛出的异常统一转换为 {@link ApiResponse} 输出，并根据错误分类映射 HTTP 状态码。</p>
 *
 * <h2>处理范围</h2>
 * <ul>
 *   <li>{@link GoyaException}：使用结构化错误码与消息解析输出。</li>
 *   <li>{@link MethodArgumentNotValidException}/{@link BindException}：Spring MVC 参数绑定/校验错误。</li>
 *   <li>{@link ConstraintViolationException}：方法参数约束校验错误（@Validated）。</li>
 *   <li>{@link HttpMessageNotReadableException}：请求体解析失败（JSON 格式错误等）。</li>
 *   <li>{@link MissingServletRequestParameterException}：缺少必填请求参数。</li>
 *   <li>兜底 {@link Throwable}：系统未知错误。</li>
 * </ul>
 *
 * <h2>安全性</h2>
 * <p>对外输出只使用“安全文案”，不会直接返回堆栈或 debugMessage。</p>
 *
 * @author goya
 * @since 2026/1/24 14:00
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorMessageResolver messageResolver;
    private final HttpStatusMapper statusMapper;
    private final TraceIdResolver traceIdResolver;
    private final ServletErrorProperties props;
    private final ProblemDetailFactory problemFactory;
    private final Masker masker;

    /**
     * 处理 Goya 结构化异常。
     *
     * @param ex      GoyaException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(GoyaException.class)
    public ResponseEntity<?> handleGoyaException(GoyaException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        ErrorCode code = ex.errorCode();
        String message = messageResolver.resolve(ex);

        logBySeverity(ex, traceId, request);

        HttpStatus status = resolveHttpStatus(code);
        return respond(status, code, message, traceId);
    }

    /**
     * 处理 Spring MVC 参数对象校验异常（@Valid）。
     *
     * @param ex      MethodArgumentNotValidException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        // 日志：可包含 rejectedValue，但必须脱敏
        List<Map<String, Object>> logFields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorToMap)
                .toList();
        log.warn("参数校验失败 traceId={}, path={}, errors={}",
                traceId, request.getRequestURI(), masker.mask(logFields));

        // 响应：对外只返回 field + message（不返回 rejectedValue）
        List<ApiFieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ApiFieldError.of(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorCode code = CommonErrorCode.INVALID_PARAM;
        String message = messageResolver.resolve(code, null);
        HttpStatus status = statusMapper.map(code.category());

        return respondWithFieldErrors(status, code, message, traceId, errors);
    }

    /**
     * 处理 Spring MVC 参数绑定异常。
     *
     * @param ex      BindException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBindException(BindException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        List<Map<String, Object>> logFields = ex.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorToMap)
                .toList();
        log.warn("参数绑定失败 traceId={}, path={}, errors={}",
                traceId, request.getRequestURI(), masker.mask(logFields));

        List<ApiFieldError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> ApiFieldError.of(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorCode code = CommonErrorCode.INVALID_PARAM;
        String message = messageResolver.resolve(code, null);
        HttpStatus status = statusMapper.map(code.category());

        return respondWithFieldErrors(status, code, message, traceId, errors);
    }

    /**
     * 处理方法参数约束校验异常（@Validated）。
     *
     * @param ex      ConstraintViolationException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        List<Map<String, Object>> logViolations = ex.getConstraintViolations().stream()
                .map(this::violationToMap)
                .toList();
        log.warn("参数约束校验失败 traceId={}, path={}, violations={}",
                traceId, request.getRequestURI(), masker.mask(logViolations));

        // propertyPath 更像“字段路径”，直接作为 field
        List<ApiFieldError> errors = ex.getConstraintViolations().stream()
                .map(v -> ApiFieldError.of(String.valueOf(v.getPropertyPath()), v.getMessage()))
                .toList();

        ErrorCode code = CommonErrorCode.INVALID_PARAM;
        String message = messageResolver.resolve(code, null);
        HttpStatus status = statusMapper.map(code.category());

        return respondWithFieldErrors(status, code, message, traceId, errors);
    }

    /**
     * 处理请求体解析失败（JSON 格式错误等）。
     *
     * @param ex      HttpMessageNotReadableException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        log.warn("请求体解析失败 traceId={}, path={}, msg={}",
                traceId, request.getRequestURI(), masker.mask(ex.getMessage()));

        ErrorCode code = CommonErrorCode.INVALID_PARAM;
        String message = messageResolver.resolve(code, null);

        HttpStatus status = statusMapper.map(code.category());
        return respond(status, code, message, traceId);
    }

    /**
     * 处理缺少必填请求参数。
     *
     * @param ex      MissingServletRequestParameterException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<?> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                  HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        log.warn("缺少请求参数 traceId={}, path={}, name={}",
                traceId, request.getRequestURI(), masker.mask(ex.getParameterName()));

        ErrorCode code = CommonErrorCode.INVALID_PARAM;
        String message = messageResolver.resolve(code, null);

        HttpStatus status = statusMapper.map(code.category());
        return respond(status, code, message, traceId);
    }

    /**
     * 处理不支持的媒体类型。
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<?> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex,
                                                         HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        log.warn("不支持的媒体类型 traceId={}, path={}, contentType={}",
                traceId, request.getRequestURI(), masker.mask(String.valueOf(ex.getContentType())));

        ErrorCode code = CommonErrorCode.INVALID_PARAM;
        String message = messageResolver.resolve(code, null);

        HttpStatus status = statusMapper.map(code.category());
        return respond(status, code, message, traceId);
    }

    /**
     * 处理静态资源未找到异常
     * 对于 .well-known 路径的请求，返回 404 而不是 ERROR（这是 Chrome DevTools 等工具的自动请求）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        log.warn("资源未找 traceId={}, path={}",
                traceId, request.getRequestURI());

        ErrorCode code = CommonErrorCode.NOT_FOUND;
        String message = messageResolver.resolve(code, null);

        HttpStatus status = statusMapper.map(code.category());
        return respond(status, code, message, traceId);
    }


    /**
     * 统一输出“带字段错误列表”的失败响应。
     *
     * <p>API 模式：写入 ApiResponse.fieldErrors</p>
     * <p>PROBLEM/BOTH 模式：写入 ProblemDetail 扩展属性 errors</p>
     *
     * @param status      HTTP 状态码
     * @param code        错误码
     * @param message     对外文案
     * @param traceId     traceId
     * @param fieldErrors 字段错误列表（可为空）
     * @return ResponseEntity
     */
    private ResponseEntity<?> respondWithFieldErrors(HttpStatus status,
                                                     ErrorCode code,
                                                     String message,
                                                     String traceId,
                                                     List<ApiFieldError> fieldErrors) {
        List<ApiFieldError> safeErrors = (fieldErrors == null) ? List.of() : List.copyOf(fieldErrors);

        return switch (props.responseStyle()) {
            case API -> ResponseEntity.status(status).body(
                    ApiResponse
                            .failBuilder(code)
                            .message(message)
                            .traceId(traceId)
                            // 对外默认不返回 rejectedValue，避免泄漏；如需返回可后续加开关
                            .fieldErrors(safeErrors.stream()
                                    .map(e -> new ApiFieldError(e.field(), e.message(), null))
                                    .toList())
                            .build()
            );
            case PROBLEM, BOTH -> {
                ProblemDetail pd = problemFactory.fromCode(code, message, traceId);
                // 标准 ProblemDetail 不含字段错误，这里用扩展属性承载
                pd.setProperty("errors", safeErrors.stream()
                        .map(e -> Map.of("field", e.field(), "message", e.message()))
                        .toList());
                yield ResponseEntity.status(status).body(pd);
            }
        };
    }

    /**
     * 兜底处理所有未知异常。
     *
     * @param ex      Throwable
     * @param request 当前请求
     * @return ResponseEntity
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<?> handleThrowable(Throwable ex, HttpServletRequest request) {
        String traceId = traceIdResolver.resolve(request);

        log.error("未知异常 traceId={}, path={}", traceId, request.getRequestURI(), ex);

        ErrorCode code = CommonErrorCode.SYSTEM_ERROR;
        String message = messageResolver.resolve(code, null);

        HttpStatus status = statusMapper.map(code.category());
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
        return switch (props.responseStyle()) {
            case API -> ResponseEntity.status(status).body(ApiResponse.fail(code, message, traceId));
            case PROBLEM, BOTH -> ResponseEntity.status(status).body(problemFactory.fromCode(code, message, traceId));
        };
    }

    /**
     * AUTH 分类下对 401/403 做细分：UNAUTHORIZED=401，FORBIDDEN=403。
     *
     * @param code 错误码
     * @return HTTP 状态码
     */
    private HttpStatus resolveHttpStatus(ErrorCode code) {
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
     * @param ex      异常
     * @param traceId traceId
     * @param request request
     */
    private void logBySeverity(GoyaException ex, String traceId, HttpServletRequest request) {
        Severity sev = ex.errorCode().severity();
        String uri = request.getRequestURI();
        Object maskedMeta = masker.mask(ex.metadata());

        switch (sev) {
            case INFO -> log.info("业务异常(INFO) traceId={}, uri={}, code={}, category={}, metadata={}",
                    traceId, uri, ex.errorCode().code(), ex.errorCode().category(), maskedMeta);
            case WARN -> log.warn("业务异常(WARN) traceId={}, uri={}, code={}, category={}, metadata={}",
                    traceId, uri, ex.errorCode().code(), ex.errorCode().category(), maskedMeta);
            case ERROR -> log.error("业务异常(ERROR) traceId={}, uri={}, code={}, category={}, metadata={}",
                    traceId, uri, ex.errorCode().code(), ex.errorCode().category(), maskedMeta, ex);
            case FATAL -> log.error("业务异常(FATAL) traceId={}, uri={}, code={}, category={}, metadata={}",
                    traceId, uri, ex.errorCode().code(), ex.errorCode().category(), maskedMeta, ex);
            default -> log.error("Unexpected value: {}", sev, ex);
        }
    }

    /**
     * 将 FieldError 转为 Map（用于日志），并保留被拒绝值（会被 masker 二次脱敏）。
     *
     * @param fe FieldError
     * @return map
     */
    private Map<String, Object> fieldErrorToMap(FieldError fe) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("field", fe.getField());
        m.put("rejectedValue", fe.getRejectedValue());
        m.put("message", fe.getDefaultMessage());
        return m;
    }

    /**
     * 将 ConstraintViolation 转为 Map（用于日志）。
     *
     * @param v violation
     * @return map
     */
    private Map<String, Object> violationToMap(ConstraintViolation<?> v) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("path", String.valueOf(v.getPropertyPath()));
        m.put("invalidValue", v.getInvalidValue());
        m.put("message", v.getMessage());
        return m;
    }
}
