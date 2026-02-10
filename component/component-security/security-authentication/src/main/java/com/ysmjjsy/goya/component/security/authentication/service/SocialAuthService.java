package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.error.SecurityErrorCode;
import com.ysmjjsy.goya.component.security.core.exception.SecurityAuthenticationException;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.social.service.ThirdPartService;
import jakarta.servlet.http.HttpServletRequest;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>社交认证服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SocialAuthService {

    private final ObjectProvider<ThirdPartService> thirdPartServiceProvider;
    private final SecurityUserManager securityUserManager;
    private final PrimaryAuthIssueService primaryAuthIssueService;

    public SocialAuthService(ObjectProvider<ThirdPartService> thirdPartServiceProvider,
                             SecurityUserManager securityUserManager,
                             PrimaryAuthIssueService primaryAuthIssueService) {
        this.thirdPartServiceProvider = thirdPartServiceProvider;
        this.securityUserManager = securityUserManager;
        this.primaryAuthIssueService = primaryAuthIssueService;
    }

    public String authorizeUrl(String source) {
        ThirdPartService thirdPartService = thirdPartServiceProvider.getIfAvailable();
        if (thirdPartService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE, "第三方登录服务未配置");
        }
        return thirdPartService.getAuthorizeUrl(source);
    }

    public AuthResult callback(String source,
                               Map<String, String> callbackParams,
                               HttpServletRequest servletRequest) {
        ThirdPartService thirdPartService = thirdPartServiceProvider.getIfAvailable();
        if (thirdPartService == null) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SECURITY_SERVICE_UNAVAILABLE, "第三方登录服务未配置");
        }

        try {
            AuthCallback authCallback = toAuthCallback(callbackParams);
            AuthUser authUser = thirdPartService.login(source, authCallback);
            Map<String, Object> attributes = GoyaJson.convertValue(authUser, Map.class);
            if (attributes == null) {
                attributes = new LinkedHashMap<>();
            }

            SecurityUser securityUser = securityUserManager.thirdLoginAndSave(source, attributes);
            String tenantId = StringUtils.defaultIfBlank(securityUser.getTenantId(), "public");
            ClientTypeEnum clientType = parseClientType(callbackParams.get("client_type"), ClientTypeEnum.WEB);

            return primaryAuthIssueService.issueAfterPrimary(
                    securityUser,
                    tenantId,
                    clientType,
                    callbackParams.get("device_id"),
                    servletRequest,
                    null,
                    null,
                    null
            );
        } catch (Exception ex) {
            throw new SecurityAuthenticationException(SecurityErrorCode.SOCIAL_LOGIN_FAILED, "社交登录失败", ex);
        }
    }

    private AuthCallback toAuthCallback(Map<String, String> params) {
        AuthCallback authCallback = new AuthCallback();
        authCallback.setCode(params.get("code"));
        authCallback.setAuth_code(params.get("auth_code"));
        authCallback.setState(params.get("state"));
        authCallback.setAuthorization_code(params.get("authorization_code"));
        authCallback.setOauth_token(params.get("oauth_token"));
        authCallback.setOauth_verifier(params.get("oauth_verifier"));
        return authCallback;
    }

    private ClientTypeEnum parseClientType(String rawValue, ClientTypeEnum defaultValue) {
        if (StringUtils.isBlank(rawValue)) {
            return defaultValue;
        }
        try {
            return ClientTypeEnum.valueOf(rawValue.trim().toUpperCase());
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}
