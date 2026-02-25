package com.ysmjjsy.goya.component.security.authentication.service;

import com.ysmjjsy.goya.component.framework.core.web.UserAgent;
import com.ysmjjsy.goya.component.framework.servlet.utils.WebUtils;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.core.service.LoginRiskEvaluator;
import com.ysmjjsy.goya.component.security.core.service.SecurityRiskContext;
import com.ysmjjsy.goya.component.security.core.service.SecurityRiskDecision;
import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>风控服务</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class RiskService {

    private final LoginRiskEvaluator loginRiskEvaluator;

    public RiskService(LoginRiskEvaluator loginRiskEvaluator) {
        this.loginRiskEvaluator = loginRiskEvaluator;
    }

    public SecurityRiskDecision evaluate(SecurityUser securityUser,
                                         String tenantId,
                                         ClientTypeEnum clientType,
                                         String deviceId,
                                         HttpServletRequest request,
                                         boolean trustedDevice) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        attributes.put("trustedDevice", trustedDevice);
        attributes.put("requestUri", request.getRequestURI());

        SecurityRiskContext riskContext = new SecurityRiskContext(
                securityUser,
                tenantId,
                clientType,
                deviceId,
                WebUtils.getIp(request),
                UserAgent.userAgentParse(request),
                attributes
        );

        SecurityRiskDecision decision = loginRiskEvaluator.evaluate(riskContext);
        return decision == null ? SecurityRiskDecision.allow() : decision;
    }
}
