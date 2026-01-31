package com.ysmjjsy.goya.component.mybatisplus.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>访问上下文解析器</p>
 *
 * <p>用于在请求入口构建 AccessContext。</p>
 *
 * @author goya
 * @since 2026/1/31 13:30
 */
public interface AccessContextResolver {

    /**
     * 解析访问上下文。
     *
     * @param request 请求
     * @return 访问上下文值，若无法解析返回 null
     */
    AccessContextValue resolve(HttpServletRequest request);
}
