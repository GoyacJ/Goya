package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.slf4j.MDC;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * <p>ApiResponse traceId 注入增强器</p>
 *
 * <p>作用：把 {@link TraceMdcFilter} 写入 MDC 的 traceId，同步到响应体 {@link ApiRes#traceId()}。</p>
 *
 * <p>规则：</p>
 * <ul>
 *   <li>仅处理 ApiResponse</li>
 *   <li>仅在 ApiResponse.traceId 为空时注入，不覆盖业务手工设置的 traceId</li>
 * </ul>
 * @author goya
 * @since 2026/1/24 22:35
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class TraceApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final Tracer tracer;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiRes.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  org.springframework.http.server.ServerHttpRequest request,
                                  org.springframework.http.server.ServerHttpResponse response) {

        if (!(body instanceof ApiRes<?> api)) {
            return body;
        }

        String traceId = currentTraceId();
        if (!StringUtils.hasText(traceId)) {
            return api;
        }
        return api.withTraceId(traceId);
    }

    private String currentTraceId() {
        if (tracer != null) {
            Span span = tracer.currentSpan();
            if (span != null && span.context() != null) {
                String id = span.context().traceId();
                if (StringUtils.hasText(id)) {
                    return id;
                }
            }
        }
        return MDC.get(DefaultConst.X_TRACE_ID);
    }
}
