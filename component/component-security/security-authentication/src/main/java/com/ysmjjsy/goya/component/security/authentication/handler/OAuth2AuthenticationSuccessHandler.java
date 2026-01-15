package com.ysmjjsy.goya.component.security.authentication.handler;

import com.ysmjjsy.goya.security.authentication.request.CustomizerRequestCache;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;

/**
 * <p>OAuth2 认证成功处理器</p>
 * <p>登录成功后，从 Redis RequestCache 恢复原始授权请求，重定向回授权端点</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>从 RedisRequestCache 恢复 SavedRequest</li>
 *   <li>提取原始 /oauth2/authorize 请求参数（含 code_challenge 等 PKCE 参数）</li>
 *   <li>构建完整的授权 URL</li>
 *   <li>重定向回授权端点</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final CustomizerRequestCache requestCache;

    @Override
    @NullMarked
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        log.debug("[Goya] |- security [authentication] Authentication successful for user: {}", 
                authentication.getName());

        // 1. 从 RedisRequestCache 恢复 SavedRequest
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        String redirectUrl;
        if (savedRequest != null && StringUtils.isNotBlank(savedRequest.getRedirectUrl())) {
            // 2. 使用保存的原始授权请求 URL（包含完整的 PKCE 参数）
            redirectUrl = savedRequest.getRedirectUrl();
            log.debug("[Goya] |- security [authentication] Restored SavedRequest, redirecting to: {}", redirectUrl);

            // 3. 删除 SavedRequest（一次性使用）
            requestCache.removeRequest(request, response);
        } else {
            // 4. 如果没有保存的请求，尝试从请求参数获取 redirect_uri
            String redirectUri = request.getParameter("redirect_uri");
            if (StringUtils.isNotBlank(redirectUri)) {
                redirectUrl = redirectUri;
            } else {
                // 5. 默认重定向到授权端点（不带参数，SAS 会处理）
                redirectUrl = "/oauth2/authorize";
            }
            log.debug("[Goya] |- security [authentication] No SavedRequest found, using default redirect: {}", redirectUrl);
        }

        // 6. 重定向
        response.sendRedirect(redirectUrl);
    }
}

