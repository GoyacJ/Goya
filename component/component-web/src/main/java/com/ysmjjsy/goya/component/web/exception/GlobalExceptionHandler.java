package com.ysmjjsy.goya.component.web.exception;

import com.ysmjjsy.goya.component.common.code.IResponseCode;
import com.ysmjjsy.goya.component.common.code.ResponseCodeEnum;
import com.ysmjjsy.goya.component.common.definition.exception.AbstractRuntimeException;
import com.ysmjjsy.goya.component.common.definition.exception.IException;
import com.ysmjjsy.goya.component.common.definition.exception.SystemException;
import com.ysmjjsy.goya.component.web.response.ErrorDetail;
import com.ysmjjsy.goya.component.web.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 统一处理应用中抛出的所有异常，返回统一的响应格式。
 * 支持国际化消息，自动提取异常信息并记录日志。
 * <p>
 * 异常处理优先级（从高到低）：
 * <ol>
 *   <li>HTTP 客户端/服务端异常</li>
 *   <li>参数校验异常（MethodArgumentNotValidException, BindException）</li>
 *   <li>静态资源未找到异常</li>
 *   <li>业务异常（AbstractRuntimeException, SystemException）</li>
 *   <li>其他异常（Throwable）</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/20 22:05
 * @see IException
 * @see Response
 * @see ErrorDetail
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    /**
     * 处理 HTTP 客户端/服务端异常
     */
    @ExceptionHandler({HttpClientErrorException.class, HttpServerErrorException.class})
    public ResponseEntity<Response<Void>> handleHttpException(
            Exception ex,
            HttpServletRequest request) {
        log.warn("[HTTP Exception] [{}] -> {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex, request, ResponseCodeEnum.BAD_REQUEST);
    }

    /**
     * 处理参数校验异常（@Valid 注解）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    /**
     * 处理参数绑定异常（@ModelAttribute 绑定失败）
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Response<Void>> handleBindException(
            BindException ex,
            HttpServletRequest request) {
        return handleValidationException(ex.getBindingResult(), request);
    }

    /**
     * 统一处理参数校验异常
     */
    private ResponseEntity<Response<Void>> handleValidationException(
            BindingResult bindingResult,
            HttpServletRequest request) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        // 提取所有字段错误信息
        String errorMessage = fieldErrors.stream()
                .map(error -> {
                    String field = error.getField();
                    String message = error.getDefaultMessage();
                    // 尝试使用国际化消息
                    if (messageSource != null && StringUtils.isNotBlank(error.getCode())) {
                        try {
                            message = messageSource.getMessage(
                                    error.getCode(),
                                    error.getArguments(),
                                    message,
                                    LocaleContextHolder.getLocale()
                            );
                        } catch (Exception e) {
                            log.debug("获取国际化消息失败: {}", error.getCode(), e);
                        }
                    }
                    return String.format("%s: %s", field, message);
                })
                .collect(Collectors.joining("; "));

        log.warn("[参数校验失败] [{}] -> {}", request.getRequestURI(), errorMessage);

        Response.Builder<Void> builder = Response.<Void>builder()
                .code(ResponseCodeEnum.PARAMS_VALIDATION_ERROR)
                .path(request.getRequestURI());

        // 如果有字段错误，添加第一个字段的错误详情
        if (!fieldErrors.isEmpty()) {
            FieldError firstError = fieldErrors.get(0);
            builder.error(ErrorDetail.ofField(firstError.getField(),
                    new IllegalArgumentException(errorMessage)));
        }

        return builder.build().toResponseEntity();
    }

    /**
     * 处理静态资源未找到异常
     * <p>
     * 对于 .well-known 路径的请求，返回 404 而不是 ERROR
     * （这是 Chrome DevTools 等工具的自动请求）
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Response<Void>> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpServletRequest request) {
        String requestPath = request.getRequestURI();

        // 如果是 .well-known 路径，只记录 DEBUG 日志
        if (StringUtils.isNotBlank(requestPath) && requestPath.startsWith("/.well-known/")) {
            log.debug("[静态资源未找到] [{}] (Chrome DevTools 等工具的自动请求)", requestPath);
        } else {
            // 其他静态资源未找到，记录 WARN 日志
            log.warn("[静态资源未找到] [{}]", requestPath);
        }

        return Response.<Void>builder()
                .code(ResponseCodeEnum.RESOURCE_NOT_FOUNT_ERROR)
                .path(requestPath)
                .build()
                .toResponseEntity();
    }

    /**
     * 处理业务运行时异常
     */
    @ExceptionHandler(AbstractRuntimeException.class)
    public ResponseEntity<Response<Void>> handleRuntimeException(
            AbstractRuntimeException ex,
            HttpServletRequest request) {
        log.warn("[业务异常] [{}] -> {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex, request, ex.getCode());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Response<Void>> handleSystemException(
            SystemException ex,
            HttpServletRequest request) {
        log.warn("[系统异常] [{}] -> {}", request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(ex, request, ex.getCode());
    }

    /**
     * 处理所有其他异常（兜底处理）
     * <p>
     * 注意：此方法必须放在最后，因为 Throwable 是所有异常的父类
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Response<Void>> handleThrowable(
            Throwable ex,
            HttpServletRequest request) {
        log.error("[未知异常] [{}]", request.getRequestURI(), ex);
        return buildErrorResponse(ex, request, ResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    /**
     * 构建错误响应
     * <p>
     * 统一构建错误响应的核心方法，支持国际化消息
     *
     * @param ex      异常对象
     * @param request HTTP 请求
     * @param code    响应码（如果异常实现了 IException，会优先使用异常中的 code）
     * @return 错误响应实体
     */
    private static ResponseEntity<Response<Void>> buildErrorResponse(
            Throwable ex,
            HttpServletRequest request,
            IResponseCode code) {
        // 如果异常实现了 IException，优先使用异常中的 code
        IResponseCode responseCode = (ex instanceof IException ie)
                ? ie.getCode()
                : code;

        if (responseCode == null) {
            responseCode = ResponseCodeEnum.INTERNAL_SERVER_ERROR;
        }

        Response.Builder<Void> builder = Response.<Void>builder()
                .code(responseCode)
                .path(request.getRequestURI())
                .error(ErrorDetail.withStackTrace(ex));

        return builder.build().toResponseEntity();
    }
}