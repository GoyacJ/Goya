package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.framework.core.web.UserAgent;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.dto.AuthResult;
import com.ysmjjsy.goya.component.security.authentication.service.model.PreAuthCodePayload;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.enums.MfaType;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.service.SecurityRiskDecision;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>主认证后令牌前置发放编排</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PrimaryAuthIssueService {

    private final SecurityAuthenticationProperties securityAuthenticationProperties;
    private final RiskService riskService;
    private final MfaService mfaService;
    private final PreAuthCodeService preAuthCodeService;
    private final DeviceTrustService deviceTrustService;
    private final SecurityUserManager securityUserManager;

    public PrimaryAuthIssueService(SecurityAuthenticationProperties securityAuthenticationProperties,
                                   RiskService riskService,
                                   MfaService mfaService,
                                   PreAuthCodeService preAuthCodeService,
                                   DeviceTrustService deviceTrustService,
                                   SecurityUserManager securityUserManager) {
        this.securityAuthenticationProperties = securityAuthenticationProperties;
        this.riskService = riskService;
        this.mfaService = mfaService;
        this.preAuthCodeService = preAuthCodeService;
        this.deviceTrustService = deviceTrustService;
        this.securityUserManager = securityUserManager;
    }

    public AuthResult issueAfterPrimary(SecurityUser securityUser,
                                        String tenantId,
                                        ClientTypeEnum clientType,
                                        String requestDeviceId,
                                        HttpServletRequest request,
                                        MfaType preferredMfaType,
                                        String mfaTarget,
                                        String totpSecret) {
        ClientTypeEnum finalClientType = clientType == null ? ClientTypeEnum.WEB : clientType;
        String finalTenantId = StringUtils.isNotBlank(tenantId) ? tenantId : securityUser.getTenantId();
        String deviceId = deviceTrustService.resolveDeviceId(request, requestDeviceId, securityUser.getUserId());
        boolean trusted = deviceTrustService.isTrusted(securityUser, deviceId);

        SecurityRiskDecision riskDecision = riskService.evaluate(
                securityUser,
                finalTenantId,
                finalClientType,
                deviceId,
                request,
                trusted
        );

        String sid = "sid_" + GoyaIdUtils.fastSimpleUUID();
        PreAuthCodePayload preAuthCodePayload = PreAuthCodePayload.fromUser(
                securityUser,
                finalTenantId,
                finalClientType,
                sid,
                deviceId,
                false,
                null
        );

        if (riskDecision.requireMfa()) {
            MfaType mfaType = preferredMfaType == null ? riskDecision.mfaType() : preferredMfaType;
            String challengeId = mfaService.issueChallenge(preAuthCodePayload, mfaType, mfaTarget, totpSecret);
            return AuthResult.mfaRequired(
                    challengeId,
                    securityAuthenticationProperties.mfaChallengeTtl().toSeconds(),
                    sid,
                    StringUtils.defaultIfBlank(riskDecision.reason(), "需要MFA")
            );
        }

        PreAuthCodePayload finalPayload = preAuthCodePayload.withMfaVerified(true);
        String preAuthCode = preAuthCodeService.issue(finalPayload);

        deviceTrustService.registerOrUpdate(securityUser, deviceId, request);
        securityUserManager.recordLoginSuccess(
                securityUser.getUserId(),
                securityUser.getUsername(),
                finalTenantId,
                WebUtils.getIp(request),
                UserAgent.userAgentParse(request),
                request.getRequestURI()
        );

        return AuthResult.preAuthIssued(
                preAuthCode,
                securityAuthenticationProperties.preAuthCodeTtl().toSeconds(),
                securityAuthenticationProperties.tokenExchangeGrantType(),
                sid
        );
    }
}
