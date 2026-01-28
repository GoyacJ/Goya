package com.ysmjjsy.goya.component.mybatisplus.tenant;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContextValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>MyBatis-Plus 租户行处理器（TenantLineHandler）</p>
 * 负责为 SQL 自动追加租户列隔离条件（例如 tenant_id = 'xxx'）。
 *
 * <p><b>核心契约：</b>
 * <ul>
 *   <li>tenantId 从 {@link TenantContext} 读取</li>
 *   <li>租户列名默认 tenant_id（可通过覆盖 Bean 改写）</li>
 *   <li>支持静态忽略表（公共字典表等）</li>
 *   <li>支持动态忽略：当 mode=DEDICATED_DB 且 tenantLineEnabled=false 时，全表忽略</li>
 * </ul>
 *
 * <p><b>重要约束：</b>
 * <ul>
 *   <li>生产环境建议 requireTenant=true，确保 tenantId 不会为空</li>
 *   <li>若 tenantId 为空，本处理器的行为应与全局失败策略保持一致（通常应 fail fast）</li>
 * </ul>
 * @author goya
 * @since 2026/1/28 22:09
 */
public class GoyaTenantLineHandler implements TenantLineHandler {

    private final TenantProfileStore tenantProfileStore;

    /**
     * 静态忽略表集合（小写）。
     */
    private final Set<String> ignoredTables;

    /**
     * 租户列名（默认 tenant_id）。
     */
    private final String tenantIdColumn;

    public GoyaTenantLineHandler(TenantProfileStore tenantProfileStore) {
        this(tenantProfileStore, "tenant_id", Collections.emptySet());
    }

    public GoyaTenantLineHandler(TenantProfileStore tenantProfileStore,
                                 String tenantIdColumn,
                                 Set<String> ignoredTables) {
        this.tenantProfileStore = Objects.requireNonNull(tenantProfileStore, "tenantProfileStore 不能为空");
        this.tenantIdColumn = (tenantIdColumn == null || tenantIdColumn.isBlank()) ? "tenant_id" : tenantIdColumn;

        Set<String> set = new HashSet<>();
        if (ignoredTables != null) {
            for (String t : ignoredTables) {
                if (t != null && !t.isBlank()) {
                    set.add(t.trim().toLowerCase());
                }
            }
        }
        this.ignoredTables = Collections.unmodifiableSet(set);
    }

    /**
     * 获取租户 ID 表达式。
     *
     * @return tenantId 表达式
     */
    @Override
    public Expression getTenantId() {
        TenantContextValue v = TenantContext.get();
        if (v == null || v.tenantId() == null || v.tenantId().isBlank()) {
            // 按契约：生产 requireTenant=true 会在 filter/aspect 拦截；这里兜底返回空字符串
            return new StringValue("");
        }
        return new StringValue(v.tenantId());
    }

    /**
     * 获取租户列名。
     *
     * @return 列名
     */
    @Override
    public String getTenantIdColumn() {
        return tenantIdColumn;
    }

    /**
     * 是否忽略该表（不追加 tenant 条件）。
     *
     * @param tableName 表名
     * @return true 表示忽略
     */
    @Override
    public boolean ignoreTable(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return false;
        }
        String t = tableName.trim().toLowerCase();

        // 1) 静态忽略
        if (ignoredTables.contains(t)) {
            return true;
        }

        // 2) 动态忽略：独库且 tenantLineEnabled=false -> 全表忽略
        TenantContextValue ctx = TenantContext.get();
        if (ctx == null || ctx.tenantId() == null || ctx.tenantId().isBlank()) {
            return false;
        }
        if (ctx.mode() != TenantMode.DEDICATED_DB) {
            return false;
        }

        TenantProfile profile = tenantProfileStore.load(ctx.tenantId());
        if (profile == null) {
            // 没 profile 时保守：不忽略（继续追加 tenant 条件）
            return false;
        }
        return !profile.tenantLineEnabled();
    }
}
