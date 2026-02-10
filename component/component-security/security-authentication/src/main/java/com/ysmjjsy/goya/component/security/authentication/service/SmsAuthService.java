package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.dto.SmsLoginRequest;
import com.ysmjjsy.goya.component.security.authentication.dto.SmsSendRequest;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.service.IOtpService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * <p>短信认证服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SmsAuthService {

    private final SecurityUserManager securityUserManager;
    private final ObjectProvider<IOtpService> otpServiceProvider;
    private final PrimaryAuthIssueService primaryAuthIssueService;

    public SmsAuthService(SecurityUserManager securityUserManager,
                          ObjectProvider<IOtpService> otpServiceProvider,
                          PrimaryAuthIssueService primaryAuthIssueService) {
        this.securityUserManager = securityUserManager;
        this.otpServiceProvider = otpServiceProvider;
        this.primaryAuthIssueService = primaryAuthIssueService;
    }

    public boolean send(SmsSendRequest request) {
        if (request == null || StringUtils.isBlank(request.target())) {
            throw new SecurityAuthenticationException(SecurityErrorCode.OTP_INVALID, "发送目标不能为空");
        }

        IOtpService otpService = otpServiceProvider.getIfAvailable();
        if (otpService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE, "OTP服务未配置");
        }
        otpService.send(request.tenantId(), request.target());
        return true;
    }

    public AuthResult login(SmsLoginRequest request, HttpServletRequest servletRequest) {
        if (request == null || StringUtils.isBlank(request.phoneNumber()) || StringUtils.isBlank(request.code())) {
            throw new SecurityAuthenticationException(SecurityErrorCode.OTP_INVALID, "手机号或验证码不能为空");
        }

        IOtpService otpService = otpServiceProvider.getIfAvailable();
        if (otpService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE, "OTP服务未配置");
        }

        boolean verified = otpService.verify(request.tenantId(), request.phoneNumber(), request.code());
        if (!verified) {
            throw new SecurityAuthenticationException(SecurityErrorCode.OTP_INVALID, "验证码错误");
        }

        SecurityUser securityUser = securityUserManager.smsLoginAndSave(request.phoneNumber());
        ClientTypeEnum clientType = request.clientType() == null ? ClientTypeEnum.MOBILE_APP : request.clientType();
        return primaryAuthIssueService.issueAfterPrimary(
                securityUser,
                request.tenantId(),
                clientType,
                request.deviceId(),
                servletRequest,
                request.mfaType(),
                request.mfaTarget(),
                request.totpSecret()
        );
    }
}
