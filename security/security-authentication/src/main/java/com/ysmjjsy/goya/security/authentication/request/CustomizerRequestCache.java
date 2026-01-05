package com.ysmjjsy.goya.security.authentication.request;

import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.time.Duration;
import java.util.*;

/**
 * <p>基于Redis的RequestCache实现</p>
 * <p>实现全无状态设计，支持水平扩展</p>
 * <p>用于保存和恢复 SavedRequest，支持 OAuth2 授权流程中的请求恢复</p>
 *
 * <p>存储结构：</p>
 * <ul>
 *   <li>请求缓存：oauth2:request:{requestId} -> SavedRequestData (JSON序列化)</li>
 * </ul>
 *
 * <p>Request ID 策略：</p>
 * <ul>
 *   <li>优先使用请求参数中的 state（OAuth2 标准参数）</li>
 *   <li>如果不存在，生成 UUID 并存储在请求参数中</li>
 * </ul>
 *
 * <p>特性：</p>
 * <ul>
 *   <li>支持多节点部署（无状态）</li>
 *   <li>支持水平扩展</li>
 *   <li>自动过期清理（基于TTL）</li>
 *   <li>高性能（Redis内存存储）</li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class CustomizerRequestCache implements RequestCache {

    private final ICacheService cacheService;

    // Redis Key 前缀
    private static final String REQUEST_KEY_PREFIX = "oauth2:request:";

    // 缓存名称
    private static final String CACHE_NAME = "oauth2";

    // 过期时间配置（与授权码有效期一致）
    private static final Duration REQUEST_TTL = Duration.ofSeconds(300); // 5分钟

    // Request ID 参数名
    private static final String REQUEST_ID_PARAM = "request_id";
    private static final String STATE_PARAM = "state";

    @Override
    public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
        // 1. 生成或获取 Request ID
        String requestId = getOrGenerateRequestId(request);

        // 2. 提取请求信息
        SavedRequestData requestData = extractRequestData(request);

        // 3. 保存到 Redis
        String key = REQUEST_KEY_PREFIX + requestId;
        try {
            String value = JsonUtils.toJson(requestData);
            cacheService.put(CACHE_NAME, key, value, REQUEST_TTL);
            log.debug("[Goya] |- security [authentication] SavedRequest saved: {} -> {}", key, requestData.redirectUrl());

            // 4. 将 Request ID 添加到响应中（通过 Cookie 或请求参数）
            // 由于无状态，我们通过 Cookie 传递 Request ID
            addRequestIdToResponse(response, requestId);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to save SavedRequest: {}", key, e);
        }
    }

    @Override
    public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
        // 1. 获取 Request ID
        String requestId = getRequestId(request);
        if (StringUtils.isBlank(requestId)) {
            return null;
        }

        // 2. 从 Redis 获取 SavedRequestData
        String key = REQUEST_KEY_PREFIX + requestId;
        String value = cacheService.get(CACHE_NAME, key);

        if (value == null) {
            log.debug("[Goya] |- security [authentication] SavedRequest not found: {}", key);
            return null;
        }

        // 3. 反序列化并转换为 SavedRequest
        try {
            SavedRequestData requestData = JsonUtils.fromJson(value, SavedRequestData.class);
            return createSavedRequest(requestData);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to deserialize SavedRequest: {}", key, e);
            return null;
        }
    }

    @Override
    public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
        // Spring Security 标准实现：返回匹配的请求
        // 对于无状态场景，我们直接返回原始请求
        SavedRequest savedRequest = getRequest(request, response);
        if (savedRequest != null) {
            // 这里需要构建一个新的 HttpServletRequest，包含原始请求的参数
            // 简化实现：返回原始请求（实际应该合并参数）
            return request;
        }
        return null;
    }

    @Override
    public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
        // 1. 获取 Request ID
        String requestId = getRequestId(request);
        if (StringUtils.isBlank(requestId)) {
            return;
        }

        // 2. 从 Redis 删除
        String key = REQUEST_KEY_PREFIX + requestId;
        try {
            cacheService.evict(CACHE_NAME, key);
            log.debug("[Goya] |- security [authentication] SavedRequest removed: {}", key);
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Failed to remove SavedRequest: {}", key, e);
        }
    }

    /**
     * 获取或生成 Request ID
     * <p>优先使用 state 参数，如果不存在则生成 UUID</p>
     *
     * @param request HTTP 请求
     * @return Request ID
     */
    private String getOrGenerateRequestId(HttpServletRequest request) {
        // 1. 尝试从 state 参数获取（OAuth2 标准参数）
        String state = request.getParameter(STATE_PARAM);
        if (StringUtils.isNotBlank(state)) {
            return state;
        }

        // 2. 尝试从 request_id 参数获取
        String requestId = request.getParameter(REQUEST_ID_PARAM);
        if (StringUtils.isNotBlank(requestId)) {
            return requestId;
        }

        // 3. 尝试从 Cookie 获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REQUEST_ID_PARAM.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // 4. 生成新的 UUID
        return UUID.randomUUID().toString();
    }

    /**
     * 获取 Request ID
     *
     * @param request HTTP 请求
     * @return Request ID
     */
    private String getRequestId(HttpServletRequest request) {
        // 1. 从请求参数获取
        String requestId = request.getParameter(REQUEST_ID_PARAM);
        if (StringUtils.isNotBlank(requestId)) {
            return requestId;
        }

        // 2. 从 Cookie 获取
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REQUEST_ID_PARAM.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    /**
     * 将 Request ID 添加到响应中
     *
     * @param response HTTP 响应
     * @param requestId Request ID
     */
    private void addRequestIdToResponse(HttpServletResponse response, String requestId) {
        // 通过 Cookie 传递 Request ID（无状态兼容）
        Cookie cookie = new Cookie(REQUEST_ID_PARAM, requestId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge((int) REQUEST_TTL.getSeconds());
        // 注意：生产环境应该设置 SameSite 和 Secure 属性
        response.addCookie(cookie);
    }

    /**
     * 提取请求数据
     *
     * @param request HTTP 请求
     * @return SavedRequestData
     */
    private SavedRequestData extractRequestData(HttpServletRequest request) {
        // 1. 构建完整的请求 URL（包含查询参数）
        String redirectUrl = buildRedirectUrl(request);

        // 2. 提取请求参数
        Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());

        // 3. 提取请求头
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            headers.put(headerName, headerValues);
        }

        // 4. 提取 Cookies
        List<String> cookies = new ArrayList<>();
        Cookie[] requestCookies = request.getCookies();
        if (requestCookies != null) {
            for (Cookie cookie : requestCookies) {
                cookies.add(cookie.getName() + "=" + cookie.getValue());
            }
        }

        // 5. 提取 Locale 列表（从 Accept-Language 请求头）
        List<Locale> locales = extractLocales(request);

        return new SavedRequestData(
                redirectUrl,
                request.getMethod(),
                parameterMap,
                headers,
                cookies,
                locales
        );
    }

    /**
     * 提取请求的 Locale 列表
     * <p>从 Accept-Language 请求头解析，支持多个 Locale 的优先级排序</p>
     *
     * @param request HTTP 请求
     * @return Locale 列表，按优先级排序
     */
    private List<Locale> extractLocales(HttpServletRequest request) {
        List<Locale> locales = new ArrayList<>();

        // 1. 尝试从 Accept-Language 请求头解析
        String acceptLanguage = request.getHeader("Accept-Language");
        if (StringUtils.isNotBlank(acceptLanguage)) {
            try {
                // 解析 Accept-Language 头（格式：zh-CN,zh;q=0.9,en;q=0.8）
                String[] languageRanges = acceptLanguage.split(",");
                for (String range : languageRanges) {
                    // 移除质量值（q=0.9）
                    String localeStr = range.split(";")[0].trim();
                    if (StringUtils.isNotBlank(localeStr)) {
                        try {
                            Locale locale = Locale.forLanguageTag(localeStr);
                            if (locale != null && !locale.getLanguage().isEmpty()) {
                                locales.add(locale);
                            }
                        } catch (Exception e) {
                            log.debug("[Goya] |- security [authentication] Failed to parse locale: {}", localeStr);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("[Goya] |- security [authentication] Failed to parse Accept-Language header: {}", acceptLanguage);
            }
        }

        // 2. 如果没有从 Accept-Language 解析到，使用请求的默认 Locale
        if (locales.isEmpty()) {
            Locale requestLocale = request.getLocale();
            if (requestLocale != null) {
                locales.add(requestLocale);
            }
        }

        // 3. 如果仍然为空，添加系统默认 Locale
        if (locales.isEmpty()) {
            locales.add(Locale.getDefault());
        }

        return locales;
    }

    /**
     * 构建重定向 URL
     *
     * @param request HTTP 请求
     * @return 完整的请求 URL
     */
    private String buildRedirectUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString != null) {
            return requestURL.append("?").append(queryString).toString();
        }
        return requestURL.toString();
    }

    /**
     * 创建 SavedRequest 对象
     *
     * @param requestData SavedRequestData
     * @return SavedRequest
     */
    private SavedRequest createSavedRequest(SavedRequestData requestData) {
        return new SavedRequest() {
            @Override
            public List<Cookie> getCookies() {
                List<Cookie> cookies = new ArrayList<>();
                if (requestData.cookies() != null) {
                    for (String cookieStr : requestData.cookies()) {
                        String[] parts = cookieStr.split("=", 2);
                        if (parts.length == 2) {
                            Cookie cookie = new Cookie(parts[0], parts[1]);
                            cookies.add(cookie);
                        }
                    }
                }
                return cookies;
            }

            @Override
            public String getRedirectUrl() {
                return requestData.redirectUrl();
            }

            @Override
            public List<String> getHeaderValues(String name) {
                return requestData.headers().getOrDefault(name, Collections.emptyList());
            }

            @Override
            public Collection<String> getHeaderNames() {
                return requestData.headers().keySet();
            }

            @Override
            public List<Locale> getLocales() {
                return requestData.locales() != null ? requestData.locales() : List.of();
            }

            @Override
            public String[] getParameterValues(String name) {
                return requestData.parameterMap().get(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return requestData.parameterMap();
            }

            @Override
            public String getMethod() {
                return requestData.method();
            }
        };
    }
}

