package com.ysmjjsy.goya.component.framework.core.web;

/**
 * <p>请求信息提取器</p>
 *
 * <p>从“当前线程绑定的请求上下文”提取 HTTP 请求信息。</p>
 *
 * @author goya
 * @since 2026/1/24 23:46
 */
public interface RequestInfoExtractor {

    /**
     * 提取当前请求信息。
     *
     * @return RequestInfo；若当前无请求则返回 null
     */
    RequestInfo extract();
}