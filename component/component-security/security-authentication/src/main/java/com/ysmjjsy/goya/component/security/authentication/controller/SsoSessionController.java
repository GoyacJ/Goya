package com.ysmjjsy.goya.component.security.authentication.controller;

import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.security.authentication.dto.SsoSessionRequest;
import com.ysmjjsy.goya.component.security.authentication.service.PreAuthCodeService;
import com.ysmjjsy.goya.component.security.authentication.service.model.PreAuthCodePayload;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>SSO 登录会话桥接控制器。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@RestController
@RequestMapping("/security/login")
public class SsoSessionController implements IController {

    private final PreAuthCodeService preAuthCodeService;
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public SsoSessionController(PreAuthCodeService preAuthCodeService) {
        this.preAuthCodeService = preAuthCodeService;
    }

    @PostMapping("/session")
    public ApiRes<Map<String, Object>> establishSession(@RequestBody SsoSessionRequest request,
                                                         HttpServletRequest servletRequest,
                                                         HttpServletResponse servletResponse) {
        if (request == null || StringUtils.isBlank(request.preAuthCode())) {
            throw new SecurityAuthenticationException(SecurityErrorCode.PRE_AUTH_CODE_INVALID, "pre_auth_code 不能为空");
        }

        PreAuthCodePayload payload = preAuthCodeService.consume(request.preAuthCode())
                .orElseThrow(() -> new SecurityAuthenticationException(
                        SecurityErrorCode.PRE_AUTH_CODE_INVALID,
                        "pre_auth_code 无效或已过期"
                ));

        SecurityUser securityUser = payload.toSecurityUser();
        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
                securityUser,
                null,
                securityUser.getAuthorities()
        );

        Map<String, Object> details = new LinkedHashMap<>();
        details.put(StandardClaimNamesConst.SID, payload.sid());
        details.put(StandardClaimNamesConst.MFA, payload.mfaVerified());
        details.put(StandardClaimNamesConst.CLIENT_TYPE, payload.clientType() == null ? null : payload.clientType().name());
        details.put(StandardClaimNamesConst.CLIENT_ID, payload.clientId());
        authentication.setDetails(details);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        servletRequest.getSession(true);
        servletRequest.changeSessionId();
        servletRequest.getSession(false)
                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        String continueUri = resolveContinueUri(request, servletRequest, servletResponse);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("session_established", true);
        result.put("continue", continueUri);
        result.put("sid", payload.sid());
        return response(result);
    }

    private String resolveContinueUri(SsoSessionRequest request,
                                      HttpServletRequest servletRequest,
                                      HttpServletResponse servletResponse) {
        String configuredContinue = request.resolveContinueUri();
        if (isSafeContinueUri(configuredContinue, servletRequest)) {
            return configuredContinue;
        }

        SavedRequest savedRequest = requestCache.getRequest(servletRequest, servletResponse);
        if (savedRequest != null && isSafeContinueUri(savedRequest.getRedirectUrl(), servletRequest)) {
            return savedRequest.getRedirectUrl();
        }

        return "/oauth2/authorize";
    }

    private boolean isSafeContinueUri(String continueUri, HttpServletRequest servletRequest) {
        if (StringUtils.isBlank(continueUri)) {
            return false;
        }
        if (continueUri.startsWith("/")) {
            return true;
        }
        try {
            URI uri = URI.create(continueUri);
            return StringUtils.equalsIgnoreCase(servletRequest.getServerName(), uri.getHost());
        } catch (Exception ignored) {
            return false;
        }
    }
}
