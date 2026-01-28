package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContext;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContextValue;
import com.ysmjjsy.goya.component.mybatisplus.exception.CrossTableReferenceException;
import com.ysmjjsy.goya.component.mybatisplus.permission.cache.PermissionPredicateCacheService;
import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.CompiledPredicate;
import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.PermissionCompiler;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ResourceRegistry;
import com.ysmjjsy.goya.component.mybatisplus.permission.store.PermissionRuleStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.schema.Table;

/**
 * <p>MyBatis-Plus 动态数据权限处理器</p>
 * <p>
 * 本处理器通过 MyBatis-Plus 官方数据权限插件在 SQL 解析阶段为目标表追加权限 where 条件，
 * 不改写原 SQL，不覆盖原 where，仅以 AND 的方式追加。
 *
 * <h2>执行流程</h2>
 * <ol>
 *   <li>从 {@link TenantContext} 获取 tenantId（用于规则分片与缓存 key）</li>
 *   <li>从 {@link AccessContext} 获取 subjectId / attributes（用于规则加载与变量解析）</li>
 *   <li>通过 {@link ResourceRegistry} 解析当前表对应的逻辑资源 resource</li>
 *   <li>通过 {@link PermissionRuleStore} 加载规则集 RuleSet（tenantId, subjectId, resource）</li>
 *   <li>通过 {@link PermissionCompiler} 编译为 {@link CompiledPredicate}</li>
 *   <li>生成权限表达式并返回，交由 MP 以 AND 方式拼接到原 where 后</li>
 * </ol>
 *
 * <h2>新版处理器语义差异（必须理解）</h2>
 * <p>
 * {@link MultiDataPermissionHandler#getSqlSegment(Table, Expression, String)}：
 * <ul>
 *   <li>返回值会被 <b>追加</b> 到原 where 后（AND 拼接），不会覆盖原 where</li>
 *   <li>返回 {@code null} 表示“<b>不追加任何条件</b>”</li>
 * </ul>
 * 因此在 failClosed=true 时，不能返回 null，否则会导致权限失效（等同放行）。
 *
 * <h2>失败策略</h2>
 * <ul>
 *   <li>failClosed=true：任何上下文缺失/规则缺失/编译失败 => 追加恒假条件（1=0）</li>
 *   <li>failClosed=false：上述情况 => 返回 null（不追加）并由上层日志告警（建议实现时补日志）</li>
 * </ul>
 *
 * <h2>写操作是否应用权限</h2>
 * <p>
 *
 * @author goya
 * @since 2026/1/28 22:35
 */
@Slf4j
@RequiredArgsConstructor
public class GoyaDataPermissionHandler implements MultiDataPermissionHandler {

    private final PermissionPredicateCacheService predicateCacheService;
    private final ResourceRegistry resourceRegistry;
    private final boolean failClosed;

    /**
     * 获取数据权限 SQL 片段（新版多表数据权限入口）。
     *
     * @param table             当前处理的表（含别名）
     * @param where             原 where（可能为 null）
     * @param mappedStatementId MappedStatement Id
     * @return 需要追加的权限表达式；返回 null 表示不追加
     */
    @Override
    public Expression getSqlSegment(final Table table, final Expression where, final String mappedStatementId) {
        try {
            TenantContextValue tenant = TenantContext.get();
            if (tenant == null || isBlank(tenant.tenantId())) {
                return failOrNull();
            }

            AccessContextValue access = AccessContext.get();
            if (access == null || isBlank(access.subjectId())) {
                return failOrNull();
            }

            String tableName = normalizeTableName(table);
            if (isBlank(tableName)) {
                return failOrNull();
            }

            // 资源解析：tableName + msId -> resource
            String resource = resourceRegistry.resolveResource(tableName, mappedStatementId);
            if (isBlank(resource)) {
                // 解析不到资源：不纳入权限治理范围 => 不追加
                return null;
            }

            // 使用缓存服务：内部负责 version、load、compile、L1/L2
            CompiledPredicate compiled = predicateCacheService.getOrCompile(
                    tenant.tenantId(),
                    access.subjectId(),
                    resource,
                    access
            );

            if (compiled == null) {
                // 无规则：按 failClosed 策略处理
                return failOrNull();
            }

            Expression expr = compiled.toExpression(table);
            return expr == null ? failOrNull() : expr;

        } catch (CrossTableReferenceException ex) {
            // warn：禁止跨表字段引用，msId/tableName/resource/fieldKey（fieldKey 可以在 Explain 里记录）
            log.error(ex.getMessage(), ex);
            return failOrNull();
        } catch (Exception ex) {
            // 建议：这里记录审计/告警日志（tenantId、subjectId、msId、异常类型），但不要打印敏感参数
            log.error(ex.getMessage(), ex);
            return failOrNull();
        }
    }

    /**
     * failClosed=true 返回恒假条件（1=0），否则返回 null。
     *
     * @return Expression 或 null
     */
    private Expression failOrNull() {
        return failClosed ? alwaysFalseExpression() : null;
    }

    /**
     * 构造恒假表达式：1 = 0。
     *
     * @return 恒假表达式
     */
    private static Expression alwaysFalseExpression() {
        EqualsTo eq = new EqualsTo();
        eq.setLeftExpression(new LongValue(1));
        eq.setRightExpression(new LongValue(0));
        return eq;
    }

    /**
     * 标准化表名。
     *
     * @param table 表信息
     * @return 表名
     */
    private static String normalizeTableName(Table table) {
        if (table == null) {
            return null;
        }
        return table.getName();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
