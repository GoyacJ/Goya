package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.captcha.api.CaptchaService;
import com.ysmjjsy.goya.component.captcha.definition.Verification;
import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.core.web.UserAgent;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthCacheNames;
import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.dto.PasswordLoginRequest;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.service.ITenantService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>密码认证服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PasswordAuthService {

    private final SecurityUserManager securityUserManager;
    private final SecurityAuthenticationProperties securityAuthenticationProperties;
    private final CacheService cacheService;
    private final ObjectProvider<CaptchaService> captchaServiceProvider;
    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;
    private final ObjectProvider<ITenantService> tenantServiceProvider;
    private final PrimaryAuthIssueService primaryAuthIssueService;

    public PasswordAuthService(SecurityUserManager securityUserManager,
                               SecurityAuthenticationProperties securityAuthenticationProperties,
                               CacheService cacheService,
                               ObjectProvider<CaptchaService> captchaServiceProvider,
                               ObjectProvider<PasswordEncoder> passwordEncoderProvider,
                               ObjectProvider<ITenantService> tenantServiceProvider,
                               PrimaryAuthIssueService primaryAuthIssueService) {
        this.securityUserManager = securityUserManager;
        this.securityAuthenticationProperties = securityAuthenticationProperties;
        this.cacheService = cacheService;
        this.captchaServiceProvider = captchaServiceProvider;
        this.passwordEncoderProvider = passwordEncoderProvider;
        this.tenantServiceProvider = tenantServiceProvider;
        this.primaryAuthIssueService = primaryAuthIssueService;
    }

    public AuthResult login(PasswordLoginRequest request, HttpServletRequest servletRequest) {
        if (request == null || StringUtils.isBlank(request.username()) || StringUtils.isBlank(request.password())) {
            throw new SecurityAuthenticationException(SecurityErrorCode.AUTHENTICATION_FAILED, "用户名或密码不能为空");
        }

        String tenantId = resolveTenantId(request.tenantId(), servletRequest);
        ensureNotLocked(tenantId, request.username(), servletRequest);
        verifyCaptchaIfRequired(request);

        SecurityUser securityUser;
        try {
            securityUser = securityUserManager.findUserByUsername(request.username());
        } catch (Exception ex) {
            onPasswordFailure(tenantId, request.username(), servletRequest, ex.getMessage());
            throw new SecurityAuthenticationException(SecurityErrorCode.AUTHENTICATION_FAILED, "账号或密码错误", ex);
        }

        if (!passwordMatched(request.password(), securityUser.getPassword())) {
            onPasswordFailure(tenantId, request.username(), servletRequest, "密码不匹配");
            throw new SecurityAuthenticationException(SecurityErrorCode.AUTHENTICATION_FAILED, "账号或密码错误");
        }

        resetAttempt(tenantId, request.username(), servletRequest);

        ClientTypeEnum clientType = request.clientType() == null ? ClientTypeEnum.WEB : request.clientType();
        return primaryAuthIssueService.issueAfterPrimary(
                securityUser,
                tenantId,
                clientType,
                request.clientId(),
                request.deviceId(),
                servletRequest,
                request.mfaType(),
                request.mfaTarget(),
                request.totpSecret()
        );
    }

    private String resolveTenantId(String requestTenantId, HttpServletRequest servletRequest) {
        if (StringUtils.isNotBlank(requestTenantId)) {
            return requestTenantId;
        }

        ITenantService tenantService = tenantServiceProvider.getIfAvailable();
        if (tenantService != null) {
            String tenantId = tenantService.resolveTenantId(servletRequest);
            if (StringUtils.isNotBlank(tenantId)) {
                return tenantId;
            }
        }

        String tenantHeader = servletRequest.getHeader("X-Tenant-Id");
        if (StringUtils.isNotBlank(tenantHeader)) {
            return tenantHeader;
        }
        return "public";
    }

    private String buildAttemptKey(String tenantId, String principal, String ip) {
        return tenantId + ":" + principal + ":" + ip;
    }

    private void ensureNotLocked(String tenantId, String principal, HttpServletRequest request) {
        String key = buildAttemptKey(tenantId, principal, WebUtils.getIp(request));
        Long counter = cacheService.getCounter(SecurityAuthCacheNames.LOGIN_ATTEMPT, key);
        if (counter != null && counter >= securityAuthenticationProperties.loginAttemptThreshold()) {
            throw new SecurityAuthenticationException(SecurityErrorCode.ACCOUNT_TEMP_LOCKED, "账号已临时锁定，请稍后再试");
        }
    }

    private void onPasswordFailure(String tenantId, String principal, HttpServletRequest request, String reason) {
        String key = buildAttemptKey(tenantId, principal, WebUtils.getIp(request));
        cacheService.incrByWithTtlOnCreate(
                SecurityAuthCacheNames.LOGIN_ATTEMPT,
                key,
                1,
                securityAuthenticationProperties.loginAttemptWindow()
        );
        securityUserManager.recordLoginFailure(
                principal,
                WebUtils.getIp(request),
                UserAgent.userAgentParse(request),
                request.getRequestURI(),
                reason
        );
    }

    private void resetAttempt(String tenantId, String principal, HttpServletRequest request) {
        String key = buildAttemptKey(tenantId, principal, WebUtils.getIp(request));
        cacheService.resetCounter(SecurityAuthCacheNames.LOGIN_ATTEMPT, key);
    }

    private boolean passwordMatched(String requestPassword, String encodedPassword) {
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder != null && StringUtils.isNotBlank(encodedPassword)) {
            return passwordEncoder.matches(requestPassword, encodedPassword);
        }
        return StringUtils.equals(requestPassword, encodedPassword);
    }

    private void verifyCaptchaIfRequired(PasswordLoginRequest request) {
        if (!securityAuthenticationProperties.captchaEnabled()) {
            return;
        }

        CaptchaService captchaService = captchaServiceProvider.getIfAvailable();
        if (captchaService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_CONFIGURATION_ERROR, "验证码服务未配置");
        }

        if (StringUtils.isBlank(request.captchaIdentity())
                || request.captchaCategory() == null
                || StringUtils.isBlank(request.captchaCharacters())) {
            throw new SecurityAuthenticationException(SecurityErrorCode.CAPTCHA_INVALID, "缺少验证码参数");
        }

        Verification verification = new Verification();
        verification.setIdentity(request.captchaIdentity());
        verification.setCategory(request.captchaCategory());
        verification.setCharacters(request.captchaCharacters());
        if (!captchaService.verify(verification)) {
            throw new SecurityAuthenticationException(SecurityErrorCode.CAPTCHA_INVALID, "验证码错误");
        }
    }
}
