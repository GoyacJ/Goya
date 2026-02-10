package com.ysmjjsy.goya.component.security.oauth2.request.handler;

import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.mfa.domain.MfaChallenge;
import com.ysmjjsy.goya.component.security.authentication.mfa.domain.MfaConfig;
import com.ysmjjsy.goya.component.security.authentication.mfa.service.MfaService;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import org.springframework.beans.factory.ObjectProvider;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.oauth2.client.ClientTypeResolver;
import com.ysmjjsy.goya.component.security.oauth2.client.MobileAuthStateData;
import com.ysmjjsy.goya.component.security.oauth2.client.MobileAuthStateStore;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import com.ysmjjsy.goya.component.security.oauth2.token.TemporaryAuthTokenGenerator;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    private final RequestCache requestCache;
    private final SecurityOAuth2Properties oauth2Properties;
    private final ClientTypeResolver clientTypeResolver;
    private final TemporaryAuthTokenGenerator tempAuthTokenGenerator;
    private final MobileAuthStateStore mobileAuthStateStore;
    private final RegisteredClientRepository registeredClientRepository;
    private final org.springframework.beans.factory.ObjectProvider<MfaService> mfaServiceProvider;
    private final SecurityAuthenticationProperties authenticationProperties;

    @Override
    @NullMarked
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException {
        log.debug("[Goya] |- security [authentication] Authentication successful for user: {}", 
                authentication.getName());

        // 1. 检查 MFA 状态
        SecurityUser user = extractSecurityUser(authentication);
        MfaService mfaService = mfaServiceProvider.getIfAvailable();
        if (user != null && mfaService != null && authenticationProperties.mfa().enabled()) {
            MfaConfig mfaConfig = mfaService.getMfaConfig(user.getUserId());
            if (mfaConfig.enabled()) {
                // 用户启用了 MFA，创建挑战并返回 MFA 验证页面
                handleMfaChallenge(request, response, authentication, user, mfaConfig, mfaService);
                return;
            }
        }

        // 2. 识别客户端类型
        String clientId = request.getParameter("client_id");
        RegisteredClient client = clientId != null ? registeredClientRepository.findByClientId(clientId) : null;
        ClientTypeEnum clientType = clientTypeResolver.resolve(request, client);

        // 3. 根据客户端类型选择处理方式
        if (clientType == ClientTypeEnum.MOBILE_APP) {
            handleMobileAppAuth(request, response, authentication, client);
        } else if (clientType == ClientTypeEnum.MINIPROGRAM) {
            // 小程序已在 WxAppLoginController 处理，这里不应该到达
            log.warn("[Goya] |- security [authentication] Mini-program auth should use WxAppLoginController");
            handleWebAuth(request, response);
        } else {
            // Web 端：使用 Session + RequestCache
            handleWebAuth(request, response);
        }
    }

    /**
     * 处理 MFA 挑战
     * <p>创建 MFA 挑战并返回挑战响应</p>
     */
    @NullMarked
    private void handleMfaChallenge(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication, SecurityUser user, MfaConfig mfaConfig,
                                    MfaService mfaService) throws IOException {
        log.debug("[Goya] |- security [mfa] User {} has MFA enabled, creating challenge", user.getUserId());

        // 1. 提取 Session ID（用于关联）
        String sessionId = request.getSession(false) != null ? request.getSession().getId() : UUID.randomUUID().toString();

        // 2. 构建元数据（保存完整的 OAuth2 授权请求信息）
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ip", WebUtils.getClientIp(request));
        metadata.put("userAgent", request.getHeader("User-Agent"));
        
        // 保存用户手机号（用于 SMS MFA）
        if (user.getPhoneNumber() != null) {
            metadata.put("phoneNumber", user.getPhoneNumber());
        }
        
        // 保存 OAuth2 授权请求参数
        String clientId = request.getParameter("client_id");
        String redirectUri = request.getParameter("redirect_uri");
        String responseType = request.getParameter("response_type");
        String scope = request.getParameter("scope");
        String state = request.getParameter("state");
        String codeChallenge = request.getParameter("code_challenge");
        String codeChallengeMethod = request.getParameter("code_challenge_method");
        
        metadata.put("clientId", clientId);
        metadata.put("redirectUri", redirectUri);
        metadata.put("responseType", responseType);
        metadata.put("scope", scope);
        metadata.put("state", state);
        metadata.put("codeChallenge", codeChallenge);
        metadata.put("codeChallengeMethod", codeChallengeMethod);
        
        // 识别客户端类型（用于后续恢复流程）
        RegisteredClient client = clientId != null ? registeredClientRepository.findByClientId(clientId) : null;
        ClientTypeEnum clientType = clientTypeResolver.resolve(request, client);
        metadata.put("clientType", clientType.name());
        
        // 对于 Web 端，尝试保存 SavedRequest 的引用
        if (clientType == ClientTypeEnum.WEB) {
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null && StringUtils.isNotBlank(savedRequest.getRedirectUrl())) {
                metadata.put("savedRequestUrl", savedRequest.getRedirectUrl());
            }
        }
        
        // 对于移动端，生成或获取 state 并保存到 MobileAuthStateStore
        if (clientType == ClientTypeEnum.MOBILE_APP) {
            if (StringUtils.isBlank(state)) {
                state = UUID.randomUUID().toString();
            }
            // 保存移动端授权状态
            Map<String, String> additionalParams = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (!isStandardOAuth2Param(key)) {
                    additionalParams.put(key, values.length > 0 ? values[0] : "");
                }
            });
            MobileAuthStateData authStateData = new MobileAuthStateData(
                    clientId,
                    redirectUri,
                    responseType != null ? responseType : "code",
                    scope,
                    state,
                    codeChallenge,
                    codeChallengeMethod,
                    additionalParams
            );
            mobileAuthStateStore.saveAuthState(state, authStateData);
            metadata.put("state", state);
        }

        // 3. 创建 MFA 挑战
        com.ysmjjsy.goya.component.security.core.enums.MfaType requiredType = mfaConfig.type() != null
                ? mfaConfig.type()
                : com.ysmjjsy.goya.component.security.core.enums.MfaType.valueOf(
                        authenticationProperties.mfa().defaultType());
        
        MfaChallenge challenge = mfaService.createChallenge(
                user.getUserId(),
                user.getUsername(),
                requiredType,
                sessionId,
                metadata
        );

        // 4. 返回 MFA 挑战响应
        String tenantId = (String) request.getAttribute(
                com.ysmjjsy.goya.component.security.oauth2.tenant.TenantRequestAttributes.ATTR_TENANT_ID);
        String prefix = tenantId != null ? "/t/" + tenantId : "";

        // 5. 根据客户端类型返回不同的响应
        String clientId = request.getParameter("client_id");
        RegisteredClient client = clientId != null ? registeredClientRepository.findByClientId(clientId) : null;
        ClientTypeEnum clientType = clientTypeResolver.resolve(request, client);

        if (clientType == ClientTypeEnum.MOBILE_APP) {
            // 移动端：返回 JSON 响应
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            PrintWriter writer = response.getWriter();
            writer.write(String.format(
                    "{\"mfa_required\":true,\"challenge_id\":\"%s\",\"type\":\"%s\",\"message\":\"需要 MFA 验证\"}",
                    challenge.challengeId(),
                    requiredType.name()
            ));
            writer.flush();
        } else {
            // Web 端：重定向到 MFA 验证页面
            String mfaVerifyUrl = prefix + "/mfa/verify?challenge_id=" + challenge.challengeId();
            if (StringUtils.isNotBlank(clientId)) {
                mfaVerifyUrl += "&client_id=" + clientId;
            }
            response.sendRedirect(mfaVerifyUrl);
        }
    }

    /**
     * 从 Authentication 中提取 SecurityUser
     */
    @NullMarked
    private SecurityUser extractSecurityUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser) {
            return securityUser;
        }
        return null;
    }

    /**
     * 处理移动端认证（无 Session）
     */
    @NullMarked
    private void handleMobileAppAuth(HttpServletRequest request, HttpServletResponse response,
                                     Authentication authentication, RegisteredClient client) throws IOException {
        log.debug("[Goya] |- security [authentication] Handling mobile app authentication");

        // 1. 生成临时认证 Token
        String tempToken = tempAuthTokenGenerator.generate(authentication, ClientTypeEnum.MOBILE_APP.name());

        // 2. 提取授权请求参数
        String clientId = request.getParameter("client_id");
        String redirectUri = request.getParameter("redirect_uri");
        String responseType = request.getParameter("response_type");
        String scope = request.getParameter("scope");
        String codeChallenge = request.getParameter("code_challenge");
        String codeChallengeMethod = request.getParameter("code_challenge_method");

        // 3. 生成 State（如果不存在）
        String state = request.getParameter("state");
        if (StringUtils.isBlank(state)) {
            state = UUID.randomUUID().toString();
        }

        // 4. 保存授权请求状态到 Redis
        Map<String, String> additionalParams = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (!isStandardOAuth2Param(key)) {
                additionalParams.put(key, values.length > 0 ? values[0] : "");
            }
        });

        MobileAuthStateData authStateData = new MobileAuthStateData(
                clientId,
                redirectUri,
                responseType != null ? responseType : "code",
                scope,
                state,
                codeChallenge,
                codeChallengeMethod,
                additionalParams
        );

        mobileAuthStateStore.saveAuthState(state, authStateData);

        // 5. 构建授权端点 URL（携带临时 Token 和 state）
        String tenantId = (String) request.getAttribute(
                com.ysmjjsy.goya.component.security.oauth2.tenant.TenantRequestAttributes.ATTR_TENANT_ID);
        String prefix = tenantId != null ? "/t/" + tenantId : "";

        StringBuilder authorizeUrl = new StringBuilder(prefix + "/oauth2/authorize");
        authorizeUrl.append("?client_id=").append(clientId != null ? clientId : "");
        authorizeUrl.append("&response_type=code");
        authorizeUrl.append("&state=").append(state);
        authorizeUrl.append("&temp_token=").append(tempToken);
        if (StringUtils.isNotBlank(redirectUri)) {
            authorizeUrl.append("&redirect_uri=").append(java.net.URLEncoder.encode(redirectUri, java.nio.charset.StandardCharsets.UTF_8));
        }
        if (StringUtils.isNotBlank(codeChallenge)) {
            authorizeUrl.append("&code_challenge=").append(codeChallenge);
        }
        if (StringUtils.isNotBlank(codeChallengeMethod)) {
            authorizeUrl.append("&code_challenge_method=").append(codeChallengeMethod);
        }
        if (StringUtils.isNotBlank(scope)) {
            authorizeUrl.append("&scope=").append(java.net.URLEncoder.encode(scope, java.nio.charset.StandardCharsets.UTF_8));
        }

        log.debug("[Goya] |- security [authentication] Mobile app auth state saved, redirecting to authorize endpoint");
        response.sendRedirect(authorizeUrl.toString());
    }

    /**
     * 处理 Web 端认证（使用 Session）
     */
    @NullMarked
    private void handleWebAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.debug("[Goya] |- security [authentication] Handling web authentication");

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
            String tenantId = (String) request.getAttribute(
                    com.ysmjjsy.goya.component.security.oauth2.tenant.TenantRequestAttributes.ATTR_TENANT_ID);
            String prefix = tenantId != null ? "/t/" + tenantId : "";
            // 4. 如果没有保存的请求，尝试从请求参数获取 redirect_uri
            String redirectUri = request.getParameter("redirect_uri");
            if (StringUtils.isNotBlank(redirectUri)) {
                redirectUrl = redirectUri;
            } else {
                // 5. 默认重定向到授权端点（不带参数，SAS 会处理）
                redirectUrl = prefix + "/oauth2/authorize";
            }
            log.debug("[Goya] |- security [authentication] No SavedRequest found, using default redirect: {}", redirectUrl);
        }

        // 6. 重定向
        response.sendRedirect(redirectUrl);
    }

    /**
     * 判断是否为标准 OAuth2 参数
     */
    private boolean isStandardOAuth2Param(String paramName) {
        return "client_id".equals(paramName) ||
                "redirect_uri".equals(paramName) ||
                "response_type".equals(paramName) ||
                "scope".equals(paramName) ||
                "state".equals(paramName) ||
                "code_challenge".equals(paramName) ||
                "code_challenge_method".equals(paramName);
    }

    /**
     * 判断是否为自定义 URL Scheme
     *
     * @param uri URI 字符串
     * @return true 如果是自定义 URL Scheme，false 否则
     */
    private boolean isCustomUrlScheme(String uri) {
        if (StringUtils.isBlank(uri)) {
            return false;
        }
        try {
            URI parsedUri = URI.create(uri);
            String scheme = parsedUri.getScheme();
            // 自定义 URL Scheme 通常不是 http/https
            return scheme != null && !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https");
        } catch (Exception e) {
            log.debug("[Goya] |- security [authentication] Failed to parse URI: {}", uri, e);
            return false;
        }
    }

    /**
     * 验证 URL Scheme 是否在白名单中
     *
     * @param uri URI 字符串
     * @return true 如果在白名单中，false 否则
     */
    private boolean isAllowedUrlScheme(String uri) {
        if (oauth2Properties == null || oauth2Properties.mobile() == null) {
            log.warn("[Goya] |- security [authentication] OAuth2 mobile config not available");
            return false;
        }

        List<String> allowedSchemes = oauth2Properties.mobile().urlSchemes();
        if (allowedSchemes == null || allowedSchemes.isEmpty()) {
            log.warn("[Goya] |- security [authentication] No URL schemes configured, rejecting: {}", uri);
            return false;
        }

        try {
            URI parsedUri = URI.create(uri);
            String scheme = parsedUri.getScheme();
            if (scheme == null) {
                return false;
            }

            // 检查是否匹配白名单中的任何一个 URL Scheme
            String uriScheme = scheme + "://";
            for (String allowedScheme : allowedSchemes) {
                if (uriScheme.startsWith(allowedScheme) || allowedScheme.equals(uriScheme)) {
                    log.debug("[Goya] |- security [authentication] URL Scheme allowed: {} matches {}", uri, allowedScheme);
                    return true;
                }
            }

            log.warn("[Goya] |- security [authentication] URL Scheme not in whitelist: {}", uri);
            return false;
        } catch (Exception e) {
            log.warn("[Goya] |- security [authentication] Failed to validate URL Scheme: {}", uri, e);
            return false;
        }
    }
}
