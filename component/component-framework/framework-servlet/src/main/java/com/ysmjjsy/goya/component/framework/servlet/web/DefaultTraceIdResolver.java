package com.ysmjjsy.goya.component.framework.servlet.web;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * <p>默认 TraceId 解析器实现。</p>
 *
 * <p>读取顺序：</p>
 * <ol>
 *   <li>Micrometer {@link Tracer} 当前 span 的 traceId（若 tracer 存在且当前 span 存在）</li>
 *   <li>MDC（默认 key 为 traceId）</li>
 *   <li>请求头（默认 X-Trace-Id，可配置）</li>
 * </ol>
 *
 * @author goya
 * @since 2026/1/24 13:52
 */
public class DefaultTraceIdResolver implements TraceIdResolver {

    /**
     * 默认 MDC key。
     */
    public static final String DEFAULT_MDC_KEY = "traceId";

    private final Tracer tracer;
    private final String mdcKey;
    private final String headerName;

    /**
     * 构造默认 TraceIdResolver。
     *
     * @param tracer Micrometer Tracer（可为空）
     * @param headerName 请求头名称（不能为空）
     */
    public DefaultTraceIdResolver(Tracer tracer, String headerName) {
        this(tracer, DEFAULT_MDC_KEY, headerName);
    }

    /**
     * 构造默认 TraceIdResolver（支持自定义 MDC key）。
     *
     * @param tracer Micrometer Tracer（可为空）
     * @param mdcKey MDC key（不能为空）
     * @param headerName 请求头名称（不能为空）
     */
    public DefaultTraceIdResolver(Tracer tracer, String mdcKey, String headerName) {
        this.tracer = tracer;
        this.mdcKey = Objects.requireNonNull(mdcKey, "mdcKey 不能为空");
        this.headerName = Objects.requireNonNull(headerName, "headerName 不能为空");
    }

    /** {@inheritDoc} */
    @Override
    public String resolve(HttpServletRequest request) {
        Objects.requireNonNull(request, "request 不能为空");

        // 1) micrometer tracing
        if (tracer != null) {
            Span span = tracer.currentSpan();
            if (span != null ) {
                String tid = span.context().traceId();
                if (StringUtils.hasText(tid)) {
                    return tid;
                }
            }
        }

        // 2) MDC
        String tid = MDC.get(mdcKey);
        if (StringUtils.hasText(tid)) {
            return tid;
        }

        // 3) header
        tid = request.getHeader(headerName);
        return StringUtils.hasText(tid) ? tid : null;
    }
}
