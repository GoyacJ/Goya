package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.dto.WxMiniLoginRequest;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.social.service.WxMiniProgramService;
import com.ysmjjsy.goya.component.social.service.dto.WxAppLoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

/**
 * <p>微信小程序认证服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class WxMiniProgramAuthService {

    private final ObjectProvider<WxMiniProgramService> wxMiniProgramServiceProvider;
    private final SecurityUserManager securityUserManager;
    private final PrimaryAuthIssueService primaryAuthIssueService;

    public WxMiniProgramAuthService(ObjectProvider<WxMiniProgramService> wxMiniProgramServiceProvider,
                                    SecurityUserManager securityUserManager,
                                    PrimaryAuthIssueService primaryAuthIssueService) {
        this.wxMiniProgramServiceProvider = wxMiniProgramServiceProvider;
        this.securityUserManager = securityUserManager;
        this.primaryAuthIssueService = primaryAuthIssueService;
    }

    public AuthResult login(WxMiniLoginRequest request, HttpServletRequest servletRequest) {
        if (request == null || StringUtils.isBlank(request.appId()) || StringUtils.isBlank(request.code())) {
            throw new SecurityAuthenticationException(SecurityErrorCode.WX_MINI_LOGIN_FAILED, "appId或code不能为空");
        }

        WxMiniProgramService wxMiniProgramService = wxMiniProgramServiceProvider.getIfAvailable();
        if (wxMiniProgramService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE, "微信小程序服务未配置");
        }

        try {
            WxAppLoginResponse loginResponse = wxMiniProgramService.login(request.code(), request.appId());
            SecurityUser securityUser = securityUserManager.wxAppLoginAndSave(
                    loginResponse.openid(),
                    request.appId(),
                    loginResponse.sessionKey(),
                    request.encryptedData(),
                    request.iv()
            );

            ClientTypeEnum clientType = request.clientType() == null ? ClientTypeEnum.MINIPROGRAM : request.clientType();
            String tenantId = StringUtils.defaultIfBlank(request.tenantId(), securityUser.getTenantId());

            return primaryAuthIssueService.issueAfterPrimary(
                    securityUser,
                    tenantId,
                    clientType,
                    request.deviceId(),
                    servletRequest,
                    request.mfaType(),
                    request.mfaTarget(),
                    request.totpSecret()
            );
        } catch (Exception ex) {
            throw new SecurityAuthenticationException(SecurityErrorCode.WX_MINI_LOGIN_FAILED, "小程序登录失败", ex);
        }
    }
}
