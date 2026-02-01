package com.ysmjjsy.goya.component.framework.security.service;

import com.ysmjjsy.goya.component.framework.security.api.AuthorizationService;
import com.ysmjjsy.goya.component.framework.security.api.AuthorizeRequest;
import com.ysmjjsy.goya.component.framework.security.context.ResourceResolver;
import com.ysmjjsy.goya.component.framework.security.context.SubjectResolver;
import com.ysmjjsy.goya.component.framework.security.decision.*;
import com.ysmjjsy.goya.component.framework.security.domain.Policy;
import com.ysmjjsy.goya.component.framework.security.domain.PolicyQuery;
import com.ysmjjsy.goya.component.framework.security.domain.Resource;
import com.ysmjjsy.goya.component.framework.security.domain.Subject;
import com.ysmjjsy.goya.component.framework.security.spi.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;

/**
 * <p>默认鉴权服务实现，提供核心流程编排。</p>
 *
 * @author goya
 * @since 2026/1/31 10:20
 */
@RequiredArgsConstructor
public class DefaultAuthorizationService implements AuthorizationService {

    private final SubjectResolver subjectResolver;
    private final ResourceResolver resourceResolver;
    private final PolicyRepository policyRepository;
    private final PolicyEngine policyEngine;

    @Override
    public Decision authorize(@NonNull AuthorizeRequest request) {
        Subject subject = subjectResolver.resolve(request.getSubjectContext());
        if (subject == null) {
            return Decision.deny("主体解析失败");
        }
        Resource resource = resourceResolver.resolve(request.getResourceContext());
        if (resource == null) {
            return Decision.deny("资源解析失败");
        }
        if (request.getAction() == null) {
            return Decision.deny("操作为空");
        }

        DecisionContext context = buildDecisionContext(request, subject, resource);
        return policyEngine.evaluate(context);
    }

    @Override
    public DecisionExplain authorizeWithExplain(AuthorizeRequest request) {
        if (request == null) {
            return DecisionExplain.denyExplain("请求为空");
        }
        Subject subject = subjectResolver.resolve(request.getSubjectContext());
        if (subject == null) {
            return DecisionExplain.denyExplain("主体解析失败");
        }
        Resource resource = resourceResolver.resolve(request.getResourceContext());
        if (resource == null) {
            return DecisionExplain.denyExplain("资源解析失败");
        }
        if (request.getAction() == null) {
            return DecisionExplain.denyExplain("操作为空");
        }

        DecisionContext context = buildDecisionContext(request, subject, resource);
        return policyEngine.evaluateWithExplain(context);
    }

    /**
     * 构建决策上下文
     *
     * @param request  鉴权请求
     * @param subject  主体
     * @param resource 资源
     * @return 决策上下文
     */
    private DecisionContext buildDecisionContext(AuthorizeRequest request, Subject subject, Resource resource) {
        DecisionContext context = new DecisionContext();
        context.setTenantCode(request.getTenantCode());
        context.setSubject(subject);
        context.setResource(resource);
        context.setAction(request.getAction());
        context.setEnvironment(request.getEnvironment());
        context.setRequestTime(request.getRequestTime());
        context.setPolicies(loadPolicies(request, subject, resource));
        return context;
    }

    /**
     * 加载授权策略信息
     *
     * @param request  鉴权请求
     * @param subject  主体
     * @param resource 资源
     * @return 授权策略信息
     */
    private List<Policy> loadPolicies(AuthorizeRequest request, Subject subject, Resource resource) {
        PolicyQuery query = new PolicyQuery();
        query.setTenantCode(request.getTenantCode());
        query.setSubject(subject);
        query.setResource(resource);
        query.setAction(request.getAction());
        query.setRequestTime(request.getRequestTime());
        query.setEnvironment(request.getEnvironment());
        List<Policy> policies = policyRepository.findEffectivePolicies(query);
        return policies == null ? Collections.emptyList() : policies;
    }
}
