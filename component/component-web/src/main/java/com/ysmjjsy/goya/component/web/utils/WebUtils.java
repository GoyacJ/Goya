package com.ysmjjsy.goya.component.web.utils;

import com.google.common.collect.Maps;
import com.ysmjjsy.goya.component.common.definition.constants.IBaseConstants;
import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.common.enums.ProtocolEnum;
import com.ysmjjsy.goya.component.common.utils.*;
import com.ysmjjsy.goya.component.web.constants.IWebConstants;
import com.ysmjjsy.goya.component.web.response.Response;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:00
 */
@Slf4j
@UtilityClass
public class WebUtils {


    private static final String[] SESSION_IDS = new String[]{"JSESSIONID, SESSION"};
    public static final String IP_CONSTANT = "0:0:0:0:0:0:0:1";


    /**
     * 判断请求是否为 HTML 类型
     *
     * @param request 请求对象 {@link HttpServletRequest}
     * @return true 请求体数据类型为 html，false 请求体数据类型不是 html
     */
    public static boolean isHtml(HttpServletRequest request) {
        String accept = getAccept(request);
        String contentType = getContentType(request);
        return isHtml(accept, contentType);
    }

    /**
     * 判断请求是否为 HTML，例如通过浏览器直接访问的。
     *
     * @param accept      Accept 请求头
     * @param contentType Content Type 请求头
     * @return true 请求体数据类型为 text/html，false 请求体数据类型不是 application/json
     */
    public static Boolean isHtml(String accept, String contentType) {
        if (StringUtils.isNotBlank(contentType) && Strings.CI.equals(MediaType.TEXT_HTML_VALUE, contentType)) {
            return true;
        } else {
            return Strings.CS.containsAny(accept, MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_XHTML_XML_VALUE);
        }
    }

    /**
     * 获取头信息
     *
     * @param httpHeaders {@link HttpHeaders}
     * @param name        头名称
     * @return 头信息值
     */
    public static List<String> getHeaders(HttpHeaders httpHeaders, String name) {
        return httpHeaders.get(name);
    }

    /**
     * 获取头信息
     *
     * @param serverHttpRequest {@link ServerHttpRequest}
     * @param name              名称
     * @return 头信息值
     */
    public static List<String> getHeaders(ServerHttpRequest serverHttpRequest, String name) {
        return getHeaders(serverHttpRequest.getHeaders(), name);
    }

    /**
     * 获取指定请求头的值，如果头部为空则返回空字符串
     *
     * @param request 请求对象
     * @param name    头部名称
     * @return 头部值
     */
    public static String getHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (StringUtils.isEmpty(value)) {
            return StringUtils.EMPTY;
        }
        return urlDecode(value);
    }

    /**
     * 获取第一条头信息
     *
     * @param httpHeaders {@link HttpHeaders}
     * @param name        头名称
     * @return 如果存在就返回第一条头信息值，如果不存在就返回空。
     */
    public static String getFirstHeader(HttpHeaders httpHeaders, String name) {
        List<String> values = getHeaders(httpHeaders, name);
        return CollectionUtils.isNotEmpty(values) ? values.getFirst() : null;
    }

    /**
     * 获取第一条头信息
     *
     * @param serverHttpRequest {@link ServerHttpRequest}
     * @param name              名称
     * @return 如果存在就返回第一条头信息值，如果不存在就返回空。
     */
    public static String getFirstHeader(ServerHttpRequest serverHttpRequest, String name) {
        return getFirstHeader(serverHttpRequest.getHeaders(), name);
    }

    /**
     * 获取头信息
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @param name               名称
     * @return 头信息值
     */
    public static String getFirstHeader(HttpServletRequest httpServletRequest, String name) {
        return httpServletRequest.getHeader(name);
    }

    /**
     * 请求头中是否存在某个 Header
     *
     * @param httpHeaders {@link HttpHeaders}
     * @param name        头名称
     * @return true 存在，false 不存在
     */
    public static boolean hasHeader(HttpHeaders httpHeaders, String name) {
        return httpHeaders.containsHeader(name);
    }

    /**
     * 请求头中是否存在某个 Header
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @param name               名称
     * @return true 存在，false 不存在
     */
    public static Boolean hasHeader(HttpServletRequest httpServletRequest, String name) {
        return StringUtils.isNotBlank(getFirstHeader(httpServletRequest, name));
    }

    /**
     * 请求头中是否存在某个 Header
     *
     * @param serverHttpRequest {@link ServerHttpRequest}
     * @param name              名称
     * @return true 存在，false 不存在
     */
    public static Boolean hasHeader(ServerHttpRequest serverHttpRequest, String name) {
        return hasHeader(serverHttpRequest.getHeaders(), name);
    }

    /**
     * 获取 COOKIE 头请求头内容
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return COOKIE 请求头内容
     */
    public static String getCookie(HttpServletRequest httpServletRequest) {
        return getFirstHeader(httpServletRequest, HttpHeaders.COOKIE);
    }

    /**
     * 获取 COOKIE 请求头内容
     *
     * @param serverHttpRequest {@link ServerHttpRequest}
     * @return COOKIE 请求头内容
     */
    public static String getCookie(ServerHttpRequest serverHttpRequest) {
        return getFirstHeader(serverHttpRequest, HttpHeaders.COOKIE);
    }

    /**
     * 获取 COOKIE 请求头内容
     *
     * @param httpInputMessage {@link HttpInputMessage}
     * @return COOKIE 请求头内容
     */
    public static String getCookie(HttpInputMessage httpInputMessage) {
        return getFirstHeader(httpInputMessage.getHeaders(), HttpHeaders.COOKIE);
    }

    /**
     * 获取 AUTHORIZATION 请求头内容
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return AUTHORIZATION 请求头或者为空
     */
    public static String getAuthorization(HttpServletRequest httpServletRequest) {
        return getFirstHeader(httpServletRequest, HttpHeaders.AUTHORIZATION);
    }

    /**
     * 获取 Bearer Token 的值
     *
     * @param request {@link HttpServletRequest}
     * @return 如果 AUTHORIZATION 不存在，或者 Token 不是以 “Bearer ” 开头，则返回 null。如果 AUTHORIZATION 存在，而且是以 “Bearer ” 开头，那么返回 “Bearer ” 后面的值。
     */
    public static String getBearerToken(HttpServletRequest request) {
        String header = getAuthorization(request);
        if (StringUtils.isNotBlank(header) && Strings.CS.startsWith(header, IBaseConstants.BEARER_TOKEN)) {
            return Strings.CS.remove(header, IBaseConstants.BEARER_TOKEN);
        } else {
            return null;
        }
    }

    /**
     * 获取 ORIGIN 请求头内容
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return ORIGIN 请求头或者为空
     */
    public static String getOrigin(HttpServletRequest httpServletRequest) {
        return getFirstHeader(httpServletRequest, HttpHeaders.ORIGIN);
    }

    /**
     * 获取 ACCEPT 请求头内容
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return ACCEPT 请求头或者为空
     */
    public static String getAccept(HttpServletRequest httpServletRequest) {
        return getFirstHeader(httpServletRequest, HttpHeaders.ACCEPT);
    }

    /**
     * 获取 CONTENT_TYPE 请求头内容
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return CONTENT_TYPE 请求头或者为空
     */
    public static String getContentType(HttpServletRequest httpServletRequest) {
        return getFirstHeader(httpServletRequest, HttpHeaders.CONTENT_TYPE);
    }

    public static String getIp(HttpServletRequest httpServletRequest) {
        String ip = getClientIp(httpServletRequest);
        if (Strings.CS.equals(ip, IP_CONSTANT)) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return IBaseConstants.LOCAL_HOST_IP;
            }
        } else {
            return ip;
        }
    }

    /**
     * 从 Cookie 请求头中，找到给定任意给定属性的值
     *
     * @param serverHttpRequest {@link ServerHttpRequest}
     * @param name              Cookie中的属性名
     * @return 如果 Cookie 存在属性名就返回对应的值，如果不存在则返回null
     */
    public static String getAnyFromCookieHeader(ServerHttpRequest serverHttpRequest, String... name) {
        String cookie = getCookie(serverHttpRequest);
        return getAny(cookie, name);
    }

    /**
     * 获取 Session ID
     *
     * @param httpInputMessage {@link HttpInputMessage}
     * @return session ID 或者 null
     */
    public static String getSessionIdFromHeader(HttpInputMessage httpInputMessage) {
        return getAnyFromCookieHeader(httpInputMessage, SESSION_IDS);
    }

    /**
     * 从 Cookie 请求头中，找到给定任意给定属性的值
     *
     * @param httpInputMessage {@link HttpInputMessage}
     * @param name             Cookie中的属性名
     * @return 如果 Cookie 存在属性名就返回对应的值，如果不存在则返回null
     */
    public static String getAnyFromCookieHeader(HttpInputMessage httpInputMessage, String... name) {
        String cookie = getCookie(httpInputMessage);
        return getAny(cookie, name);
    }


    /**
     * 从 Cookie 请求头中，找到给定任意给定属性的值
     *
     * @param cookie Cookie 请求头值
     * @param name   Cookie中的属性名
     * @return cookie 中属性值的集合
     */
    public static String getAny(String cookie, String... name) {
        List<String> result = get(cookie, name);
        return CollectionUtils.isNotEmpty(result) ? result.get(0) : null;
    }

    /**
     * 获取多个 Cookie 请求头中的属性值
     *
     * @param cookie Cookie 请求头值
     * @param name   Cookie中的属性名
     * @return cookie 中属性值的集合
     */
    public static List<String> get(String cookie, String... name) {
        Map<String, String> cookies = rawCookieToMap(cookie);
        return Stream.of(name).map(cookies::get).toList();
    }

    /**
     * 解析 Cookie 头的值解析为 Map
     *
     * @param cookie Cookie 头的值
     * @return Cookie 键值对。
     */
    private static Map<String, String> rawCookieToMap(String cookie) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(cookie)) {
            return Stream.of(cookie.split(ISymbolConstants.SEMICOLON_AND_SPACE))
                    .map(pair -> pair.split(ISymbolConstants.EQUAL))
                    .collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * 获取 Session Id。
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @param create             create 是否创建新的 Session
     * @return ids 或者 null
     */
    public static String getSessionId(HttpServletRequest httpServletRequest, boolean create) {
        HttpSession httpSession = getSession(httpServletRequest, create);
        return ObjectUtils.isNotEmpty(httpSession) ? httpSession.getId() : null;
    }

    /**
     * 获取 Session ID。
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return session ID 或者 null
     */
    public static String getSessionId(HttpServletRequest httpServletRequest) {
        return getSessionId(httpServletRequest, false);
    }

    /**
     * 获取 Session ID
     *
     * @param serverHttpRequest {@link ServerHttpRequest}
     * @return session ID 或者 null
     */
    public static String getSessionIdFromHeader(ServerHttpRequest serverHttpRequest) {
        return getAnyFromCookieHeader(serverHttpRequest, SESSION_IDS);
    }

    public static String getRequestId(ServerHttpRequest request) {
        String sessionId = getSessionIdFromHeader(request);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = getFirstHeader(request, IWebConstants.HEADER_REQUEST_ID);
        }
        return sessionId;
    }

    public static String getRequestId(HttpServletRequest request) {
        String sessionId = getSessionId(request);
        if (StringUtils.isBlank(sessionId)) {
            sessionId = getFirstHeader(request, IWebConstants.HEADER_REQUEST_ID);
        }
        return sessionId;
    }

    public static String getRequestId(HttpInputMessage httpInputMessage) {
        String sessionId = getSessionIdFromHeader(httpInputMessage);
        if (org.apache.commons.lang3.StringUtils.isBlank(sessionId)) {
            sessionId = getFirstHeader(httpInputMessage.getHeaders(), IWebConstants.HEADER_REQUEST_ID);
        }
        return sessionId;
    }

    /**
     * 判断基于 request 的前后端数据加密是否开启
     *
     * @param httpInputMessage {@link HttpInputMessage}
     * @param requestId        requestId
     * @return true 已开启，false 未开启。
     */
    public static boolean isCryptoEnabled(HttpInputMessage httpInputMessage, String requestId) {
        return hasHeader(httpInputMessage.getHeaders(), IWebConstants.HEADER_REQUEST_ID) && StringUtils.isNotBlank(requestId);
    }

    public static boolean isCryptoEnabled(HttpServletRequest httpServletRequest, String sessionId) {
        return hasHeader(httpServletRequest, IWebConstants.HEADER_REQUEST_ID) && StringUtils.isNotBlank(sessionId);
    }

    /**
     * 将 getSession 统一封装为一个方法，方便统一修改
     * <p>
     * 该方法默认不创建新的 getSession
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return {@link HttpSession} or null
     */
    public static HttpSession getSession(HttpServletRequest httpServletRequest) {
        return getSession(httpServletRequest, false);
    }

    /**
     * 将 getSession 统一封装为一个方法，方便统一修改
     *
     * @param httpServletRequest httpServletRequest {@link HttpServletRequest}
     * @param create             是否创建新的 Session
     * @return {@link HttpSession}
     */
    public static HttpSession getSession(HttpServletRequest httpServletRequest, boolean create) {
        return httpServletRequest.getSession(create);
    }

    /**
     * 获取自定义 HEADER_GOYA_TENANT_ID 请求头内容
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return HEADER_GOYA_TENANT_ID 请求头内容
     */
    public static String getTenantId(HttpServletRequest httpServletRequest) {
        return getFirstHeader(httpServletRequest, IWebConstants.HEADER_TENANT_ID);
    }

    /**
     * 将内容写入到响应
     *
     * @param response    响应 {@link HttpServletResponse}
     * @param statusCode  状态码
     * @param content     待写入的内容
     * @param contentType 内容类型
     */
    public static void render(HttpServletResponse response, int statusCode, String content, String contentType) {
        try {
            response.setStatus(statusCode);
            response.setContentType(contentType);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.getWriter().print(content);
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            log.error("[GOYA] |- Render response error!");
        }
    }

    /**
     * 将 JSON 写入到响应。
     *
     * @param response   响应 {@link HttpServletResponse}
     * @param statusCode 状态码
     * @param content    待写入的内容
     */
    public static void renderJson(HttpServletResponse response, int statusCode, String content) {
        render(response, statusCode, content, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 将 JSON 写入到响应。
     *
     * @param response   响应 {@link HttpServletResponse}
     * @param statusCode 状态码
     * @param object     待写入的内容
     */
    public static void renderJson(HttpServletResponse response, int statusCode, Object object) {
        renderJson(response, statusCode, JsonUtils.toJson(object));
    }

    /**
     * 将 Result 以 JSON 格式输出到响应。
     *
     * @param response 响应 {@link HttpServletResponse}
     * @param result   待写入的内容 {@link Response}
     */
    public static void renderResult(HttpServletResponse response, Response<Void> result) {
        renderJson(response, result.httpStatus().value(), result);
    }

    /**
     * 将 HTML 写入到响应。
     *
     * @param response   响应 {@link HttpServletResponse}
     * @param statusCode 状态码
     * @param content    待写入的内容
     */
    public static void renderHtml(HttpServletResponse response, int statusCode, String content) {
        render(response, statusCode, content, MediaType.TEXT_HTML_VALUE);
    }

    /* ---------- Spring 家族配置属性 ---------- */

    public static String getApplicationName(ApplicationContext applicationContext) {
        return PropertyResolverUtils.getProperty(applicationContext.getEnvironment(), IBaseConstants.PROPERTY_SPRING_APPLICATION_NAME);
    }

    public static String getContextPath(ApplicationContext applicationContext) {
        return PropertyResolverUtils.getProperty(applicationContext.getEnvironment(), IBaseConstants.PROPERTY_SERVER_CONTEXT_PATH);
    }

    /**
     * 获取指定名称的 String 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    /**
     * 获取指定名称的 String 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static String getParameter(String name, String defaultValue) {
        return ConvertUtils.toStr(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Integer getParameterToInt(String name) {
        return ConvertUtils.toInt(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的 Integer 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Integer getParameterToInt(String name, Integer defaultValue) {
        return ConvertUtils.toInt(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数
     *
     * @param name 参数名
     * @return 参数值
     */
    public static Boolean getParameterToBool(String name) {
        return ConvertUtils.toBool(getRequest().getParameter(name));
    }

    /**
     * 获取指定名称的 Boolean 类型的请求参数，若参数不存在，则返回默认值
     *
     * @param name         参数名
     * @param defaultValue 默认值
     * @return 参数值或默认值
     */
    public static Boolean getParameterToBool(String name, Boolean defaultValue) {
        return ConvertUtils.toBool(getRequest().getParameter(name), defaultValue);
    }

    /**
     * 获取所有请求参数（以 Map 的形式返回）
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 请求参数的 Map，键为参数名，值为参数值数组
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获取所有请求参数（以 Map 的形式返回，值为字符串形式的拼接）
     *
     * @param request 请求对象{@link ServletRequest}
     * @return 请求参数的 Map，键为参数名，值为拼接后的字符串
     */
    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String> params = Maps.newHashMap();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), CommonUtils.joinComma(entry.getValue()));
        }
        return params;
    }

    /**
     * 获取当前 HTTP 请求对象
     *
     * @return 当前 HTTP 请求对象
     */
    public static HttpServletRequest getRequest() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前 HTTP 响应对象
     *
     * @return 当前 HTTP 响应对象
     */
    public static HttpServletResponse getResponse() {
        try {
            return getRequestAttributes().getResponse();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前请求的 HttpSession 对象
     * <p>
     * 如果当前请求已经关联了一个会话（即已经存在有效的 session ID），
     * 则返回该会话对象；如果没有关联会话，则会创建一个新的会话对象并返回。
     * <p>
     * HttpSession 用于存储会话级别的数据，如用户登录信息、购物车内容等，
     * 可以在多个请求之间共享会话数据
     *
     * @return 当前请求的 HttpSession 对象
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 获取当前请求的请求属性
     *
     * @return {@link ServletRequestAttributes} 请求属性对象
     */
    public static ServletRequestAttributes getRequestAttributes() {
        try {
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            return (ServletRequestAttributes) attributes;
        } catch (Exception e) {
            return null;
        }
    }



    /**
     * 获取所有请求头的 Map，键为头部名称，值为头部值
     *
     * @param request 请求对象
     * @return 请求头的 Map
     */
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedCaseInsensitiveMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * 将字符串渲染到客户端（以 JSON 格式返回）
     *
     * @param response 渲染对象
     * @param string   待渲染的字符串
     */
    public static void renderString(HttpServletResponse response, String string) {
        try {
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.toString());
            response.getWriter().print(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断当前请求是否为 Ajax 异步请求
     *
     * @param request 请求对象
     * @return 是否为 Ajax 请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {

        // 判断 Accept 头部是否包含 application/json
        String accept = request.getHeader("accept");
        if (accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        // 判断 X-Requested-With 头部是否包含 XMLHttpRequest
        String xRequestedWith = request.getHeader("X-Requested-With");
        if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
            return true;
        }

        // 判断 URI 后缀是否为 .json 或 .xml
        String uri = request.getRequestURI();
        if (StringUtils.equalsAnyIgnoreCase(uri, ".json", ".xml")) {
            return true;
        }

        // 判断请求参数 __ajax 是否为 json 或 xml
        String ajax = request.getParameter("__ajax");
        return StringUtils.equalsAnyIgnoreCase(ajax, "json", "xml");
    }

    /**
     * 获取客户端 IP 地址
     * <p>按优先级检查以下请求头：</p>
     * <ol>
     *   <li>X-Forwarded-For：代理服务器转发的原始客户端 IP（可能包含多个 IP，取第一个）</li>
     *   <li>X-Real-IP：Nginx 等代理服务器设置的原始客户端 IP</li>
     *   <li>Proxy-Client-IP：Apache 代理服务器</li>
     *   <li>WL-Proxy-Client-IP：WebLogic 代理服务器</li>
     *   <li>HTTP_CLIENT_IP：部分代理服务器</li>
     *   <li>HTTP_X_FORWARDED_FOR：部分代理服务器</li>
     *   <li>request.getRemoteAddr()：直接连接的客户端 IP</li>
     * </ol>
     *
     * @return 客户端 IP 地址，如果无法获取则返回 "unknown"
     */
    public static String getClientIp() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "unknown";
        }
        return getClientIp(request);
    }

    /**
     * 获取客户端 IP 地址
     *
     * @param request HTTP 请求对象
     * @return 客户端 IP 地址，如果无法获取则返回 "unknown"
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        // 按优先级检查各种请求头（IP 地址不需要 URL 解码，直接获取原始值）
        String ip = request.getHeader("X-Forwarded-For");
        if (NetUtils.isValidIp(ip)) {
            // X-Forwarded-For 可能包含多个 IP，格式：client, proxy1, proxy2
            // 取第一个非 unknown 的 IP
            String[] ips = ip.split(",");
            for (String ipAddr : ips) {
                ipAddr = ipAddr.trim();
                if (NetUtils.isValidIp(ipAddr) && !"unknown".equalsIgnoreCase(ipAddr)) {
                    return ipAddr;
                }
            }
        }

        ip = request.getHeader("X-Real-IP");
        if (NetUtils.isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (NetUtils.isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (NetUtils.isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (NetUtils.isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (NetUtils.isValidIp(ip)) {
            return ip;
        }

        // 最后使用 request.getRemoteAddr()
        ip = request.getRemoteAddr();
        if (NetUtils.isValidIp(ip)) {
            // 处理 IPv6 本地地址，转换为实际的本地 IP
            if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                try {
                    return InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    log.warn("[GOYA] |- Failed to get local host address: {}", e.getMessage());
                    return "127.0.0.1";
                }
            }
            return ip;
        }

        return "unknown";
    }

    /**
     * 对内容进行 URL 编码
     *
     * @param str 内容
     * @return 编码后的内容
     */
    public static String urlEncode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    /**
     * 对内容进行 URL 解码
     *
     * @param str 内容
     * @return 解码后的内容
     */
    public static String urlDecode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }

    /**
     * 符合语法规则的 URL
     * <p>
     * 检测地址相关字符串是否以"/"结尾，如果没有就帮助增加一个 ""/""
     *
     * @param url http 请求地址字符串
     * @return 结构合理的请求地址字符串
     */
    public static String url(String url) {
        if (Strings.CS.endsWith(url, ISymbolConstants.FORWARD_SLASH)) {
            return url;
        } else {
            return url + ISymbolConstants.FORWARD_SLASH;
        }
    }

    /**
     * 符合语法规则的 ParentId
     * <p>
     * 树形结构 ParentId 健壮性校验方法。
     *
     * @param parentId 父节点ID
     * @return 格式友好的 parentId
     */
    public static String parentId(String parentId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(parentId)) {
            return IBaseConstants.TREE_ROOT_ID;
        } else {
            return parentId;
        }
    }

    /**
     * 将IP地址加端口号，转换为http地址。
     *
     * @param address             ip地址加端口号，格式：ip:port
     * @param protocol            http协议类型 {@link ProtocolEnum}
     * @param endWithForwardSlash 是否在结尾添加“/”
     * @return http格式地址
     */
    public static String addressToUri(String address, ProtocolEnum protocol, boolean endWithForwardSlash) {
        StringBuilder stringBuilder = new StringBuilder();

        if (!Strings.CS.startsWith(address, protocol.getFormat())) {
            stringBuilder.append(protocol.getFormat());
        }

        if (endWithForwardSlash) {
            stringBuilder.append(url(address));
        } else {
            stringBuilder.append(address);
        }

        return stringBuilder.toString();
    }

    /**
     * 将IP地址加端口号，转换为http地址。
     *
     * @param address             ip地址加端口号，格式：ip:port
     * @param endWithForwardSlash 是否在结尾添加“/”
     * @return http格式地址
     */
    public static String addressToUri(String address, boolean endWithForwardSlash) {
        return addressToUri(address, ProtocolEnum.HTTP, endWithForwardSlash);
    }

    /**
     * 将IP地址加端口号，转换为http地址。
     *
     * @param address ip地址加端口号，格式：ip:port
     * @return http格式地址
     */
    public static String addressToUri(String address) {
        return addressToUri(address, false);
    }

    /**
     * 获取运行主机ip地址
     *
     * @return ip地址，或者null
     */
    public static String getHostAddress() {
        InetAddress address;
        try {
            address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("[GOYA] |- Get host address error: {}", e.getLocalizedMessage());
            return null;
        }
    }

    public static String serviceUri(String serviceUri, String serviceName, String gatewayServiceUri, String abbreviation) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(serviceUri)) {
            return serviceUri;
        } else {
            if (org.apache.commons.lang3.StringUtils.isBlank(serviceName)) {
                log.error("[GOYA] |- Property [{} Service Name] is not set or property format is incorrect!", abbreviation);
                throw new CommonException();
            } else {
                if (org.apache.commons.lang3.StringUtils.isBlank(gatewayServiceUri)) {
                    log.error("[GOYA] |- Property [gateway-service-uri] is not set or property format is incorrect!");
                    throw new CommonException();
                } else {
                    return url(gatewayServiceUri) + serviceName;
                }
            }
        }
    }

    public static String sasUri(String uri, String endpoint, String issuerUri) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(uri)) {
            return uri;
        } else {
            if (org.apache.commons.lang3.StringUtils.isBlank(issuerUri)) {
                log.error("[GOYA] |- Property [issuer-uri] is not set or property format is incorrect!");
                throw new CommonException();
            } else {
                return issuerUri + endpoint;
            }
        }
    }

    /**
     * 含字符的字符串鲁棒性校验。
     *
     * @param content      字符串内容
     * @param symbol       指定的字符
     * @param isStartsWith 开头还是结尾：true 字符串开头；false 字符串结尾
     * @param isRetain     是否保留：true 保留，没有该字符就加上；false 去除，有该字符则去掉
     * @return 健壮的字符串
     */
    public static String robustness(String content, String symbol, boolean isStartsWith, boolean isRetain) {
        if (isStartsWith) {
            if (isRetain) {
                if (Strings.CS.startsWith(content, symbol)) {
                    return content;
                } else {
                    return symbol + content;
                }
            } else {
                if (Strings.CS.startsWith(content, symbol)) {
                    return Strings.CS.removeStart(content, symbol);
                } else {
                    return content;
                }
            }
        } else {
            if (isRetain) {
                if (Strings.CS.endsWith(content, symbol)) {
                    return content;
                } else {
                    return content + symbol;
                }
            } else {
                if (Strings.CS.endsWith(content, symbol)) {
                    return Strings.CS.removeEnd(content, symbol);
                } else {
                    return content;
                }
            }
        }
    }

    public static UserAgent getUserAgent(HttpServletRequest request) {
        return UserAgent.parse(request);
    }
}
