package com.ysmjjsy.goya.component.security.authentication.controller;

import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.security.authentication.auth.*;
import com.ysmjjsy.goya.component.security.authentication.dto.*;
import com.ysmjjsy.goya.component.security.authentication.service.*;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>统一认证入口</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/security/auth")
public class SecurityAuthController implements IController {

    private final PasswordAuthService passwordAuthService;
    private final SmsAuthService smsAuthService;
    private final SocialAuthService socialAuthService;
    private final WxMiniProgramAuthService wxMiniProgramAuthService;
    private final MfaService mfaService;
    @Qualifier("securityAuthenticationManager")
    private final AuthenticationManager securityAuthenticationManager;

    @PostMapping("/password/login")
    public ApiRes<AuthResult> passwordLogin(@RequestBody PasswordLoginRequest request,
                                            HttpServletRequest servletRequest) {
        return response(authenticate(new PasswordAuthenticationToken(request, servletRequest)));
    }

    @PostMapping("/sms/send")
    public ApiRes<Boolean> smsSend(@RequestBody SmsSendRequest request) {
        return response(smsAuthService.send(request));
    }

    @PostMapping("/sms/login")
    public ApiRes<AuthResult> smsLogin(@RequestBody SmsLoginRequest request,
                                       HttpServletRequest servletRequest) {
        return response(authenticate(new SmsAuthenticationToken(request, servletRequest)));
    }

    @GetMapping("/social/{source}/authorize")
    public ApiRes<Map<String, Object>> socialAuthorize(@PathVariable("source") String source) {
        return response(Map.of("source", source, "authorize_url", socialAuthService.authorizeUrl(source)));
    }

    @GetMapping("/social/{source}/callback")
    public ApiRes<AuthResult> socialCallback(@PathVariable("source") String source,
                                             @RequestParam Map<String, String> callbackParams,
                                             HttpServletRequest servletRequest) {
        return response(authenticate(new SocialAuthenticationToken(source, callbackParams, servletRequest)));
    }

    @PostMapping("/wx-mini/login")
    public ApiRes<AuthResult> wxMiniLogin(@RequestBody WxMiniLoginRequest request,
                                          HttpServletRequest servletRequest) {
        return response(authenticate(new WxMiniProgramAuthenticationToken(request, servletRequest)));
    }

    @PostMapping("/mfa/challenge")
    public ApiRes<Map<String, Object>> mfaChallenge(@RequestBody MfaChallengeRequest request) {
        String challengeId = mfaService.issueChallenge(request);
        return response(Map.of("mfa_challenge_id", challengeId));
    }

    @PostMapping("/mfa/verify")
    public ApiRes<AuthResult> mfaVerify(@RequestBody MfaVerifyRequest request,
                                        HttpServletRequest servletRequest) {
        return response(authenticate(new MfaVerifyAuthenticationToken(request, servletRequest)));
    }

    private AuthResult authenticate(Authentication requestToken) {
        Authentication authentication = securityAuthenticationManager.authenticate(requestToken);
        if (authentication instanceof AuthResultAuthenticationToken authResultAuthenticationToken) {
            return authResultAuthenticationToken.getAuthResult();
        }
        throw new SecurityAuthenticationException(
                SecurityErrorCode.AUTHENTICATION_FAILED,
                "认证链返回了非法结果类型"
        );
    }
}
