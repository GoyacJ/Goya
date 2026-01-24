package com.ysmjjsy.goya.component.framework.core.web;

/**
 * <p>当前请求信息快照</p>
 *
 * <p>说明：若不在 Web 请求线程（例如 MQ 消费、定时任务），这些字段可能为 null。</p>
 *
 * @param requestUri 请求 URI（可选包含 queryString）
 * @param httpMethod HTTP 方法
 * @param userAgent User-Agent（可能较长，建议截断）
 * @param clientIp 客户端 IP（根据 X-Forwarded-For/X-Real-IP 等解析）
 *
 * @author goya
 * @since 2026/1/24 23:46
 */
public record RequestInfo(
        String requestUri,
        String httpMethod,
        UserAgent userAgent,
        String clientIp
) {

    public record UserAgent(
            String browserName,
            String osName,
            boolean mobile,
            boolean mobileBrowser,
            boolean iphoneOrIpod,
            boolean ipad,
            boolean ios,
            boolean android,
            String engineName
    ) {
    }
}