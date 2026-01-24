package com.ysmjjsy.goya.component.framework.servlet.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>TraceId 解析器</p>
 *
 * <p>用于在 Web 响应体与日志中输出统一的链路追踪标识。</p>
 *
 * <p>实现建议：
 * <ol>
 *   <li>优先读取 Micrometer Tracing（若启用）；</li>
 *   <li>其次读取日志 MDC；</li>
 *   <li>最后读取请求头（例如 X-Trace-Id）。</li>
 * </ol>
 * </p>
 * @author goya
 * @since 2026/1/24 13:52
 */
public interface TraceIdResolver {

    /**
     * 解析当前请求的 traceId。
     *
     * @param request 当前请求（不能为空）
     * @return traceId（可能为空）
     */
    String resolve(HttpServletRequest request);
}
