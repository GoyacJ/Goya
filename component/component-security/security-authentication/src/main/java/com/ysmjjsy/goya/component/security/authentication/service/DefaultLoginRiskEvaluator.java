package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.service.LoginRiskEvaluator;
import com.ysmjjsy.goya.component.security.core.service.SecurityRiskContext;
import com.ysmjjsy.goya.component.security.core.service.SecurityRiskDecision;

/**
 * <p>默认登录风控评估</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class DefaultLoginRiskEvaluator implements LoginRiskEvaluator {

    private final SecurityAuthenticationProperties securityAuthenticationProperties;

    public DefaultLoginRiskEvaluator(SecurityAuthenticationProperties securityAuthenticationProperties) {
        this.securityAuthenticationProperties = securityAuthenticationProperties;
    }

    @Override
    public SecurityRiskDecision evaluate(SecurityRiskContext context) {
        if (!securityAuthenticationProperties.mfaEnabled()) {
            return SecurityRiskDecision.allow();
        }

        ClientTypeEnum clientType = context.clientType();
        if (clientType != null && securityAuthenticationProperties.forceMfaClientTypesOrEmpty().contains(clientType)) {
            return SecurityRiskDecision.require(
                    securityAuthenticationProperties.defaultMfaType(),
                    "客户端策略要求MFA"
            );
        }

        Object trustedDevice = context.attributes() == null ? null : context.attributes().get("trustedDevice");
        boolean trusted = trustedDevice instanceof Boolean b && b;
        if (securityAuthenticationProperties.requireMfaForUntrustedDevice() && !trusted) {
            return SecurityRiskDecision.require(
                    securityAuthenticationProperties.defaultMfaType(),
                    "设备未信任"
            );
        }

        return SecurityRiskDecision.allow();
    }
}
