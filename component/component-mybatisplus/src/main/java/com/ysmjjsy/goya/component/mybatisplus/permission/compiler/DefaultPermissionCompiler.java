package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import com.ysmjjsy.goya.component.mybatisplus.exception.PermissionCompileException;
import com.ysmjjsy.goya.component.mybatisplus.permission.model.CombineType;
import com.ysmjjsy.goya.component.mybatisplus.permission.model.PredicateDef;
import com.ysmjjsy.goya.component.mybatisplus.permission.model.RuleDef;
import com.ysmjjsy.goya.component.mybatisplus.permission.model.RuleSet;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ColumnRef;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ResourceRegistry;
import net.sf.jsqlparser.expression.Expression;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * <p>默认权限编译器实现</p>
 * <p>
 * 支持操作符：
 * <ul>
 *   <li>EQ</li>
 *   <li>IN</li>
 *   <li>BETWEEN</li>
 *   <li>LIKE（仅允许前缀或后缀匹配：abc% 或 %abc，禁止 %abc%）</li>
 * </ul>
 *
 * <h2>变量解析</h2>
 * <ul>
 *   <li>${userId}：来自 AccessContextValue.userId()</li>
 *   <li>${xxx}：来自 AccessContextValue.attributes().get("xxx")</li>
 * </ul>
 *
 * <h2>类型校验</h2>
 * <ul>
 *   <li>EQ：1 个值</li>
 *   <li>IN：可为多值；变量可解析为 Collection/数组</li>
 *   <li>BETWEEN：2 个值，且类型必须兼容（均为 Number 或均为 String/Date/Temporal）</li>
 *   <li>LIKE：1 个字符串值，且仅允许前缀/后缀匹配</li>
 * </ul>
 *
 * <h2>规则组合策略</h2>
 * <ul>
 *   <li>单条 Rule：按 Rule.combine（AND/OR）组合其 predicates</li>
 *   <li>RuleSet 多条 Rule：默认以 OR 组合（任一规则满足即允许）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:45
 */
public class DefaultPermissionCompiler implements PermissionCompiler {

    @Override
    public CompiledPredicate compile(RuleSet ruleSet,
                                     AccessContextValue access,
                                     ResourceRegistry registry) {
        Objects.requireNonNull(ruleSet, "ruleSet 不能为空");
        Objects.requireNonNull(access, "access 不能为空");
        Objects.requireNonNull(registry, "registry 不能为空");

        Explain explain = new Explain();

        try {
            List<RuleDef> rules = ruleSet.getRules();
            if (rules == null || rules.isEmpty()) {
                explain.error("规则集为空：resource=" + ruleSet.getResource());
                throw new PermissionCompileException("规则集为空", explain);
            }

            // 优先级排序（高 -> 低）
            List<RuleDef> sorted = new ArrayList<>(rules);
            sorted.sort(Comparator.comparingInt(RuleDef::getPriority).reversed());

            AliasAwareCompiledPredicate.Node all = null;
            for (RuleDef r : sorted) {
                AliasAwareCompiledPredicate.Node one = compileRule(ruleSet, r, access, registry, explain);
                if (one == null) {
                    continue;
                }
                all = (all == null) ? one : new AliasAwareCompiledPredicate.OrNode(all, one);
            }

            if (all == null) {
                explain.error("所有规则均未生成有效表达式：resource=" + ruleSet.getResource());
                throw new PermissionCompileException("无有效规则表达式", explain);
            }

            return new AliasAwareCompiledPredicate(all, explain);
        } catch (PermissionCompileException e) {
            throw e;
        } catch (Exception e) {
            explain.error("编译异常：" + e.getClass().getSimpleName() + ":" + e.getMessage());
            throw new PermissionCompileException("规则编译失败", e, explain);
        }
    }

    private AliasAwareCompiledPredicate.Node compileRule(RuleSet ruleSet,
                                                         RuleDef rule,
                                                         AccessContextValue access,
                                                         ResourceRegistry registry,
                                                         Explain explain) {
        if (rule == null || rule.getPredicates() == null || rule.getPredicates().isEmpty()) {
            return null;
        }

        CombineType combine = rule.getCombine() == null ? CombineType.AND : rule.getCombine();

        AliasAwareCompiledPredicate.Node ruleNode = null;
        for (PredicateDef p : rule.getPredicates()) {
            AliasAwareCompiledPredicate.Node pNode = compilePredicate(ruleSet, p, access, registry, explain);
            if (ruleNode == null) {
                ruleNode = pNode;
            } else {
                ruleNode = (combine == CombineType.AND)
                        ? new AliasAwareCompiledPredicate.AndNode(ruleNode, pNode)
                        : new AliasAwareCompiledPredicate.OrNode(ruleNode, pNode);
            }
        }
        return ruleNode;
    }

    private AliasAwareCompiledPredicate.Node compilePredicate(RuleSet ruleSet,
                                                              PredicateDef p,
                                                              AccessContextValue access,
                                                              ResourceRegistry registry,
                                                              Explain explain) {
        p.validate();

        ColumnRef ref = registry.resolveColumn(ruleSet.getResource(), p.getFieldKey());

        // 变量解析与扁平化：沿用你已有 VariableResolver（返回 List<Object>）
        List<Object> resolved = VariableResolver.resolveValues(
                p.getValues(), access, explain,
                "resource=" + ruleSet.getResource() + " fieldKey=" + p.getFieldKey() + " op=" + p.getType()
        );

        // 将值转换为字面量 Expression（沿用你已有 LiteralConverter.toLiteral）
        return switch (p.getType()) {
            case EQ -> {
                if (resolved.size() != 1) {
                    explain.error("EQ 需要 1 个值，实际=" + resolved.size());
                    throw new PermissionCompileException("EQ 值数量错误", explain);
                }
                Expression v = LiteralConverter.toLiteral(resolved.getFirst(), explain, "EQ");
                yield new AliasAwareCompiledPredicate.EqNode(ref, v);
            }
            case IN -> {
                if (resolved.isEmpty()) {
                    explain.error("IN 值列表为空");
                    throw new PermissionCompileException("IN 值列表为空", explain);
                }
                List<Expression> vs = new ArrayList<>();
                for (Object o : resolved) {
                    vs.add(LiteralConverter.toLiteral(o, explain, "IN"));
                }
                yield new AliasAwareCompiledPredicate.InNode(ref, vs);
            }
            case BETWEEN -> {
                if (resolved.size() != 2) {
                    explain.error("BETWEEN 需要 2 个值，实际=" + resolved.size());
                    throw new PermissionCompileException("BETWEEN 值数量错误", explain);
                }
                if (!LiteralConverter.isBetweenCompatible(resolved.getFirst(), resolved.get(1))) {
                    explain.error("BETWEEN 两端类型不兼容");
                    throw new PermissionCompileException("BETWEEN 类型不兼容", explain);
                }
                Expression a = LiteralConverter.toLiteral(resolved.getFirst(), explain, "BETWEEN_START");
                Expression b = LiteralConverter.toLiteral(resolved.get(1), explain, "BETWEEN_END");
                yield new AliasAwareCompiledPredicate.BetweenNode(ref, a, b);
            }
            case LIKE -> {
                if (resolved.size() != 1 || !(resolved.getFirst() instanceof String)) {
                    explain.error("LIKE 仅支持 1 个字符串值");
                    throw new PermissionCompileException("LIKE 类型错误", explain);
                }
                String pattern = ((String) resolved.getFirst()).trim();
                if (!PredicateLikePolicy.isAllowed(pattern)) {
                    explain.error("LIKE 仅允许前缀/后缀匹配，pattern=" + pattern);
                    throw new PermissionCompileException("LIKE 模式不安全", explain);
                }
                Expression pat = LiteralConverter.toLiteral(pattern, explain, "LIKE");
                yield new AliasAwareCompiledPredicate.LikeNode(ref, pat);
            }
        };
    }

    /**
     * LIKE 安全策略（仅允许 abc% 或 %abc，禁止 %abc%）。
     */
    static final class PredicateLikePolicy {
        static boolean isAllowed(String pattern) {
            if (pattern == null || pattern.isEmpty()) {
                return false;
            }
            int first = pattern.indexOf('%');
            if (first < 0) {
                return true;
            }
            int last = pattern.lastIndexOf('%');
            if (first != last) {
                return false;
            }
            if (first == 0 && last == pattern.length() - 1) {
                return false;
            }
            return first == 0 || first == pattern.length() - 1;
        }
    }
}