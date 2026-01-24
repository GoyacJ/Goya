package com.ysmjjsy.goya.component.framework.servlet.web;

import com.ysmjjsy.goya.component.framework.core.web.RequestInfo;
import com.ysmjjsy.goya.component.framework.core.web.RequestInfoExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * <p>基于 Servlet 的请求信息提取器</p>
 *
 * @author goya
 * @since 2026/1/24 23:47
 */
public class ServletRequestInfoExtractor implements RequestInfoExtractor {

    @Override
    public RequestInfo extract() {
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (!(ra instanceof ServletRequestAttributes sra)) {
            return null;
        }
        HttpServletRequest req = sra.getRequest();

        String uri = buildRequestUri(req);
        String method = req.getMethod();
        RequestInfo.UserAgent ua = userAgentParse(req);
        String ip = resolveClientIp(req);

        return new RequestInfo(uri, method, ua, ip);
    }

    private String buildRequestUri(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String qs = req.getQueryString();
        if (!StringUtils.hasText(qs)) {
            return uri;
        }
        return uri + "?" + qs;
    }

    private String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int idx = xff.indexOf(',');
            String first = (idx >= 0) ? xff.substring(0, idx) : xff;
            return first.trim();
        }
        String realIp = req.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return req.getRemoteAddr();
    }

    /**
     * 从 HttpServletRequest 解析 User-Agent 信息。
     *
     * @param request HTTP 请求
     * @return UserAgent 对象，解析失败返回 null
     */
    public static RequestInfo.UserAgent userAgentParse(HttpServletRequest request) {
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

        return new RequestInfo.UserAgent(
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
