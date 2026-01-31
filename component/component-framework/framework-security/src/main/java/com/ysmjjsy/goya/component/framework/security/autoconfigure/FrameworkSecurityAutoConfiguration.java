package com.ysmjjsy.goya.component.framework.security.autoconfigure;

import com.ysmjjsy.goya.component.framework.security.api.AuthorizationService;
import com.ysmjjsy.goya.component.framework.security.context.ContextSubjectResolver;
import com.ysmjjsy.goya.component.framework.security.context.ResourceResolver;
import com.ysmjjsy.goya.component.framework.security.context.SubjectResolver;
import com.ysmjjsy.goya.component.framework.security.decision.DecisionEvaluator;
import com.ysmjjsy.goya.component.framework.security.decision.DefaultDecisionEvaluator;
import com.ysmjjsy.goya.component.framework.security.decision.DefaultPolicyEngine;
import com.ysmjjsy.goya.component.framework.security.decision.PolicyEngine;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeDslParser;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeFilterBuilder;
import com.ysmjjsy.goya.component.framework.security.service.DefaultAuthorizationService;
import com.ysmjjsy.goya.component.framework.security.spi.PolicyRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/31 19:26
 */
@Slf4j
@AutoConfiguration
public class FrameworkSecurityAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] FrameworkSecurityAutoConfiguration auto configure.");
    }

    /**
     * 默认主体解析器。
     *
     * @return SubjectResolver
     */
    @Bean
    @ConditionalOnMissingBean
    public SubjectResolver contextSubjectResolver() {
        ContextSubjectResolver contextSubjectResolver = new ContextSubjectResolver();
        log.trace("[Goya] |- component [framework] FrameworkSecurityAutoConfiguration |- bean [contextSubjectResolver] register.");
        return contextSubjectResolver;
    }

    /**
     * 决策评估器。
     *
     * @return DecisionEvaluator
     */
    @Bean
    @ConditionalOnMissingBean
    public DecisionEvaluator defaultDecisionEvaluator() {
        DefaultDecisionEvaluator defaultDecisionEvaluator = new DefaultDecisionEvaluator();
        log.trace("[Goya] |- component [framework] FrameworkSecurityAutoConfiguration |- bean [defaultDecisionEvaluator] register.");
        return defaultDecisionEvaluator;
    }

    /**
     * 策略评估引擎。
     *
     * @param evaluator 决策评估器
     * @param parser    DSL 解析器
     * @param builder   过滤器构建器
     * @return PolicyEngine
     */
    @Bean
    @ConditionalOnMissingBean
    public PolicyEngine defaultPolicyEngine(DecisionEvaluator evaluator, RangeDslParser parser, RangeFilterBuilder builder) {
        DefaultPolicyEngine defaultPolicyEngine = new DefaultPolicyEngine(evaluator, parser, builder);
        log.trace("[Goya] |- component [framework] FrameworkSecurityAutoConfiguration |- bean [defaultPolicyEngine] register.");
        return defaultPolicyEngine;
    }

    /**
     * 鉴权服务。
     *
     * @param subjectResolver  主体解析器
     * @param resourceResolver 资源解析器
     * @param policyRepository 策略仓储
     * @param policyEngine     策略引擎
     * @return AuthorizationService
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthorizationService defaultAuthorizationService(SubjectResolver subjectResolver,
                                                            ResourceResolver resourceResolver,
                                                            PolicyRepository policyRepository,
                                                            PolicyEngine policyEngine) {
        DefaultAuthorizationService defaultAuthorizationService = new DefaultAuthorizationService(subjectResolver, resourceResolver, policyRepository, policyEngine);
        log.trace("[Goya] |- component [framework] FrameworkSecurityAutoConfiguration |- bean [defaultAuthorizationService] register.");
        return defaultAuthorizationService;
    }
}
