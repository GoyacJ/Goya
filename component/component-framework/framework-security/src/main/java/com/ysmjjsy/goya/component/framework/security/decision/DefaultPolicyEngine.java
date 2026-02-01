package com.ysmjjsy.goya.component.framework.security.decision;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaStringUtils;
import com.ysmjjsy.goya.component.framework.security.domain.*;
import com.ysmjjsy.goya.component.framework.security.dsl.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>默认策略评估引擎实现。</p>
 *
 * <p>职责：过滤策略、合并决策、构建行级过滤与列级约束。</p>
 *
 * @author goya
 * @since 2026/1/31 10:20
 */
@RequiredArgsConstructor
public class DefaultPolicyEngine implements PolicyEngine {

    private final DecisionEvaluator decisionEvaluator;
    private final RangeDslParser rangeDslParser;
    private final RangeFilterBuilder rangeFilterBuilder;

    @Override
    public Decision evaluate(DecisionContext context) {
        List<Policy> policies = filterPolicies(context);
        Decision decision = decisionEvaluator.merge(policies, context);
        if (decision.getDecisionType() == DecisionType.ALLOW) {
            buildConstraints(decision, policies, context);
        }
        return decision;
    }

    @Override
    public DecisionExplain evaluateWithExplain(DecisionContext context) {
        List<Policy> policies = filterPolicies(context);
        DecisionExplain decision = decisionEvaluator.mergeWithExplain(policies, context);
        if (decision.getDecisionType() == DecisionType.ALLOW) {
            buildConstraints(decision, policies, context);
            decision.setAppliedRanges(collectRangeDsl(policies));
        }
        decision.setInheritApplied(hasInheritance(policies));
        return decision;
    }

    /**
     * 构建
     *
     * @param decision 鉴权决策结果
     * @param policies 授权策略
     * @param context  决策上下文
     */
    private void buildConstraints(Decision decision, List<Policy> policies, DecisionContext context) {
        RangeFilter rowFilter = buildRowFilter(policies, context);
        ColumnConstraint columnConstraint = buildColumnConstraint(policies);
        decision.setRowFilter(rowFilter);
        decision.setColumnConstraint(columnConstraint);
    }

    /**
     * 授权策略过滤
     *
     * @param context 决策上下文
     * @return 策略过滤
     */
    private List<Policy> filterPolicies(DecisionContext context) {
        if (CollectionUtils.isEmpty(context.getPolicies())) {
            return Collections.emptyList();
        }
        List<Policy> policies = context.getPolicies();
        return policies.stream().filter(Objects::nonNull)
                .filter(f -> isEffective(f, context.getTenantCode(), context.getRequestTime()))
                .filter(f -> isActionMatch(f, context.getAction()))
                .filter(f -> isResourceMatch(f, context.getResource()))
                .collect(Collectors.toList());
    }

    /**
     * 是否有效
     *
     * @param policy      授权策略
     * @param tenantCode  租户编码
     * @param requestTime 请求时间
     * @return 是否有效
     */
    private boolean isEffective(Policy policy, String tenantCode, LocalDateTime requestTime) {
        if (policy == null) {
            return false;
        }
        if (StringUtils.isNotBlank(policy.getTenantCode())
                && !GoyaStringUtils.equals(tenantCode, policy.getTenantCode())) {
            return false;
        }
        if (policy.isNeverExpire()) {
            return true;
        }
        if (policy.getExpireTime() == null) {
            return true;
        }
        return !policy.getExpireTime().isBefore(requestTime);
    }

    /**
     * 行为是否匹配
     *
     * @param policy 授权策略
     * @param action 行为
     * @return 是否匹配
     */
    private boolean isActionMatch(Policy policy, Action action) {
        if (policy == null || policy.getAction() == null || action == null) {
            return false;
        }
        String policyCode = policy.getAction().getCode();
        String actionCode = action.getCode();
        return GoyaStringUtils.equalsIgnoreCase(policyCode, actionCode);
    }

    /**
     * 资源是否匹配
     *
     * @param policy   授权策略
     * @param resource 资源
     * @return 是否匹配
     */
    private boolean isResourceMatch(Policy policy, Resource resource) {
        if (policy == null || resource == null) {
            return false;
        }
        if (policy.getResourceType() != null && resource.getResourceType() != null) {
            if (!GoyaStringUtils.equalsIgnoreCase(policy.getResourceType().getCode(), resource.getResourceType().getCode())) {
                return false;
            }
        }
        String policyCode = policy.getResourceCode();
        String resourceCode = resource.getResourceCode();
        if (StringUtils.isBlank(policyCode)) {
            return false;
        }

        ResourceRange range = policy.getResourceRange() == null ? ResourceRange.SELF : policy.getResourceRange();
        if (range == ResourceRange.SELF) {
            return GoyaStringUtils.equalsIgnoreCase(policyCode, resourceCode);
        }
        if (range == ResourceRange.CHILDREN) {
            return policy.isInheritFlag() && isChildResource(policyCode, resource);
        }
        if (range == ResourceRange.SELF_AND_CHILDREN) {
            if (GoyaStringUtils.equalsIgnoreCase(policyCode, resourceCode)) {
                return true;
            }
            return policy.isInheritFlag() && isChildResource(policyCode, resource);
        }
        return false;
    }

    /**
     * 是否下级资源
     *
     * @param parentCode 父资源编码
     * @param resource   资源
     * @return 是否
     */
    private boolean isChildResource(String parentCode, Resource resource) {
        if (CollectionUtils.isNotEmpty(resource.getParentCodes()) && resource.getParentCodes().contains(parentCode)) {
            return true;
        }
        return GoyaStringUtils.equalsIgnoreCase(resource.getParentCode(), parentCode);
    }

    /**
     * 构建范围过滤器标记
     *
     * @param policies 授权策略
     * @param context  决策上下文
     * @return 范围过滤器
     */
    private RangeFilter buildRowFilter(List<Policy> policies, DecisionContext context) {
        if (CollectionUtils.isEmpty(policies)) {
            return null;
        }
        List<RangeExpression> expressions = new ArrayList<>();
        for (Policy policy : policies) {
            boolean eligible =
                    policy != null
                            && policy.getPolicyEffect() == PolicyEffect.ALLOW
                            && resolvePolicyScope(policy) == PolicyScope.ROW
                            && policy.getRangeDsl() != null
                            && !policy.getRangeDsl().isBlank();
            if (eligible) {
                RangeExpression expr = rangeDslParser.parse(policy.getRangeDsl());
                if (expr != null) {
                    expressions.add(expr);
                }
            }
        }
        if (expressions.isEmpty()) {
            return null;
        }
        RangeExpression combined = combineOr(expressions);
        RangeFilterContext filterContext = new RangeFilterContext();
        filterContext.setTenantCode(context.getTenantCode());
        filterContext.setSubject(context.getSubject());
        filterContext.setResource(context.getResource());
        filterContext.setEnvironment(context.getEnvironment());

        return rangeFilterBuilder.build(combined, filterContext);
    }

    private RangeExpression combineOr(List<RangeExpression> expressions) {
        RangeExpression current = expressions.get(0);
        for (int i = 1; i < expressions.size(); i++) {
            BinaryExpression expression = new BinaryExpression();
            expression.setLeft(current);
            expression.setOperator(LogicalOperator.OR);
            expression.setRight(expressions.get(i));
            current = expression;
        }
        return current;
    }

    /**
     * 构建列级约束
     *
     * @param policies 授权策略
     * @return 列级约束
     */
    private ColumnConstraint buildColumnConstraint(List<Policy> policies) {
        if (CollectionUtils.isEmpty(policies)) {
            return null;
        }

        Set<String> allow = new HashSet<>();
        Set<String> deny = new HashSet<>();

        for (Policy p : policies) {
            boolean eligible = p != null
                    && p.getPolicyEffect() == PolicyEffect.ALLOW
                    && resolvePolicyScope(p) == PolicyScope.COLUMN;
            if (eligible) {
                List<String> a = p.getAllowColumns();
                if (CollectionUtils.isNotEmpty(a)) {
                    allow.addAll(a);
                }

                List<String> d = p.getDenyColumns();
                if (CollectionUtils.isNotEmpty(d)) {
                    deny.addAll(d);
                }
            }
        }

        ColumnConstraint constraint = new ColumnConstraint();
        constraint.setAllowColumns(allow);
        constraint.setDenyColumns(deny);
        return constraint;
    }

    private PolicyScope resolvePolicyScope(Policy policy) {
        if (policy == null) {
            return PolicyScope.RESOURCE;
        }
        if (policy.getPolicyScope() != null) {
            return policy.getPolicyScope();
        }
        boolean hasRange = StringUtils.isNotBlank(policy.getRangeDsl());
        boolean hasColumns = CollectionUtils.isNotEmpty(policy.getAllowColumns())
                || CollectionUtils.isNotEmpty(policy.getDenyColumns());
        if (hasRange) {
            return PolicyScope.ROW;
        }
        if (hasColumns) {
            return PolicyScope.COLUMN;
        }
        return PolicyScope.RESOURCE;
    }

    private List<String> collectRangeDsl(List<Policy> policies) {
        if (policies == null || policies.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> ranges = new ArrayList<>();
        for (Policy policy : policies) {
            if (policy == null || policy.getPolicyEffect() != PolicyEffect.ALLOW) {
                continue;
            }
            if (policy.getRangeDsl() != null && !policy.getRangeDsl().trim().isEmpty()) {
                ranges.add(policy.getRangeDsl());
            }
        }
        return ranges;
    }

    private boolean hasInheritance(List<Policy> policies) {
        if (policies == null) {
            return false;
        }
        for (Policy policy : policies) {
            if (policy != null && policy.isInheritFlag()) {
                return true;
            }
        }
        return false;
    }
}
