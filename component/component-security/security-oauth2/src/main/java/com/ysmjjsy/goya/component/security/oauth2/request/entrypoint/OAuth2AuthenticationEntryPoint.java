package com.ysmjjsy.goya.component.security.oauth2.request.entrypoint;

import com.ysmjjsy.goya.component.security.authentication.request.CustomizerRequestCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * <p>OAuth2 认证入口点</p>
 * <p>拦截未认证的请求，特别是 /oauth2/authorize 端点</p>
 * <p>将原始请求保存到 Redis RequestCache，然后重定向到登录页面</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>判断是否为 /oauth2/authorize 请求</li>
 *   <li>将原始请求（含 PKCE 参数）保存到 RedisRequestCache</li>
 *   <li>重定向到 /login 页面</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final CustomizerRequestCache requestCache;

    @Override
    @NullMarked
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        String requestURI = request.getRequestURI();
        log.debug("[Goya] |- security [authentication] Authentication required for: {}", requestURI);

        // 1. 判断是否为 /oauth2/authorize 请求
        if (isAuthorizationRequest(requestURI)) {
            // 2. 保存原始请求到 RedisRequestCache（包含 PKCE 参数等）
            requestCache.saveRequest(request, response);
            log.debug("[Goya] |- security [authentication] Saved authorization request to Redis: {}", requestURI);
        }

        // 3. 重定向到登录页面
        // 如果是授权请求，保留必要的上下文信息
        String loginUrl = buildLoginUrl(request);
        log.debug("[Goya] |- security [authentication] Redirecting to login: {}", loginUrl);
        response.sendRedirect(loginUrl);
    }

    /**
     * 判断是否为授权请求
     *
     * @param requestURI 请求 URI
     * @return true 如果是授权请求，false 否则
     */
    private boolean isAuthorizationRequest(String requestURI) {
        return requestURI != null && requestURI.startsWith("/oauth2/authorize");
    }

    /**
     * 构建登录 URL
     * <p>如果是授权请求，保留必要的上下文信息（如 client_id）</p>
     *
     * @param request HTTP 请求
     * @return 登录 URL
     */
    private String buildLoginUrl(HttpServletRequest request) {
        String loginUrl = "/login";

        // 如果是授权请求，保留 client_id 等参数（可选，用于前端显示）
        if (isAuthorizationRequest(request.getRequestURI())) {
            String clientId = request.getParameter("client_id");
            if (StringUtils.isNotBlank(clientId)) {
                loginUrl += "?client_id=" + clientId;
            }
        }

        return loginUrl;
    }
}

