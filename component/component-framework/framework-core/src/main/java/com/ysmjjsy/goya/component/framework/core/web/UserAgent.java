package com.ysmjjsy.goya.component.framework.core.web;

import jakarta.servlet.http.HttpServletRequest;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/2/4 23:46
 */
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

    /**
     * 从 HttpServletRequest 解析 User-Agent 信息。
     *
     * @param request HTTP 请求
     * @return UserAgent 对象，解析失败返回 null
     */
    public static UserAgent userAgentParse(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isEmpty()) {
            return null;
        }

        String browserName;
        String engineName;

        // 浏览器识别
        if (ua.contains("Chrome")) {
            browserName = "Chrome";
            engineName = "Blink";
        } else if (ua.contains("Firefox")) {
            browserName = "Firefox";
            engineName = "Gecko";
        } else if (ua.contains("Safari") && !ua.contains("Chrome")) {
            browserName = "Safari";
            engineName = "WebKit";
        } else if (ua.contains("Edge")) {
            browserName = "Edge";
            engineName = "EdgeHTML";
        } else if (ua.contains("MSIE") || ua.contains("Trident")) {
            browserName = "IE";
            engineName = "Trident";
        } else {
            browserName = "Unknown";
            engineName = "Unknown";
        }

        String osName;
        boolean mobile = false;
        boolean iphoneOrIpod = false;
        boolean ipad = false;
        boolean ios = false;
        boolean android = false;

        // 操作系统识别
        if (ua.contains("Android")) {
            osName = "Android";
            mobile = true;
            android = true;
        } else if (ua.contains("iPhone") || ua.contains("iPod")) {
            osName = "iOS";
            mobile = true;
            ios = true;
            iphoneOrIpod = true;
        } else if (ua.contains("iPad")) {
            osName = "iOS";
            mobile = true;
            ios = true;
            ipad = true;
        } else if (ua.contains("Windows")) {
            osName = "Windows";
        } else if (ua.contains("Mac OS X")) {
            osName = "Mac OS X";
        } else if (ua.contains("Linux")) {
            osName = "Linux";
        } else {
            osName = "Unknown";
        }

        boolean mobileBrowser = mobile;

        return new UserAgent(
                browserName,
                osName,
                mobile,
                mobileBrowser,
                iphoneOrIpod,
                ipad,
                ios,
                android,
                engineName
        );
    }
}
