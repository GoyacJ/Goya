package com.ysmjjsy.goya.component.framework.servlet.scan;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * <p>Rest 映射编码工具。</p>
 *
 * <p>编码规则固定为：</p>
 * <pre>
 * lower(method) + ":" + normalized(pathPattern)
 * </pre>
 *
 * @author goya
 * @since 2026/2/10
 */
public final class RestMappingCodeUtils {

    private static final String ROOT_SEGMENT = "root";

    private RestMappingCodeUtils() {
    }

    /**
     * 根据请求方法与路径模板生成资源编码。
     *
     * @param requestMethod 请求方法
     * @param pathPattern   路径模板
     * @return 资源编码
     */
    public static String createMappingCode(String requestMethod, String pathPattern) {
        String method = normalizeMethod(requestMethod);
        String path = normalizePath(pathPattern);
        return method + ":" + path;
    }

    private static String normalizeMethod(String requestMethod) {
        if (StringUtils.isBlank(requestMethod)) {
            return "all";
        }
        return requestMethod.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizePath(String pathPattern) {
        if (StringUtils.isBlank(pathPattern)) {
            return ROOT_SEGMENT;
        }

        String path = pathPattern.trim();
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        path = path.replaceAll("/+", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isBlank()) {
            return ROOT_SEGMENT;
        }

        String[] segments = path.split("/");
        List<String> normalized = new ArrayList<>(segments.length);
        for (String segment : segments) {
            if (segment == null) {
                continue;
            }
            String part = segment.trim();
            if (part.isEmpty()) {
                continue;
            }
            if (part.startsWith("{") && part.endsWith("}")) {
                normalized.add("{}");
                continue;
            }
            normalized.add(part.toLowerCase(Locale.ROOT));
        }
        if (normalized.isEmpty()) {
            return ROOT_SEGMENT;
        }
        return String.join(":", normalized);
    }
}
