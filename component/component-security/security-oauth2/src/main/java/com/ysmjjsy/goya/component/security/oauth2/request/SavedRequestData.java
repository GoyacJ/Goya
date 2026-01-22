package com.ysmjjsy.goya.component.security.oauth2.request;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>SavedRequest 数据传输对象</p>
 * <p>用于序列化 SavedRequest 的关键信息到 Redis</p>
 * <p>提取自 HttpServletRequest 的不可序列化对象</p>
 *
 * @author goya
 * @since 2025/12/21
 */
public record SavedRequestData(
        /*
          请求 URL（完整路径，包含查询参数）
         */
        String redirectUrl,

        /*
          HTTP 方法
         */
        String method,

        /*
          请求参数（Map<String, String[]>）
         */
        Map<String, String[]> parameterMap,

        /*
          请求头（Map<String, List<String>>）
         */
        Map<String, List<String>> headers,

        /*
          Cookies（List<String>，格式：name=value）
         */
        List<String> cookies,

        /*
          语言环境列表（List<Locale>）
          从 Accept-Language 请求头提取，用于国际化支持
         */
        List<Locale> locales
) implements Serializable {
}

