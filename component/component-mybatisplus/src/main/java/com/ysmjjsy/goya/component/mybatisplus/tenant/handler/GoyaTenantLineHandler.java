package com.ysmjjsy.goya.component.mybatisplus.tenant.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContextValue;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>租户列处理器</p>
 *
 * <p>用于多租户列隔离。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@RequiredArgsConstructor
public class GoyaTenantLineHandler implements TenantLineHandler {

    private final GoyaMybatisPlusProperties.Tenant options;

    /**
     * 获取租户 ID 表达式。
     *
     * @return tenantId 表达式
     */
    @Override
    public Expression getTenantId() {
        String tenantId = TenantContext.get().tenantId();
        return tenantId == null ? null : new StringValue(tenantId);
    }

    /**
     * 获取租户列名。
     *
     * @return 列名
     */
    @Override
    public String getTenantIdColumn() {
        return options.tenantIdColumn();
    }

    /**
     * 判断是否忽略表。
     *
     * @param tableName 表名
     * @return 是否忽略
     */
    @Override
    public boolean ignoreTable(String tableName) {
        TenantContextValue context = TenantContext.get();
        if (context.mode() == TenantMode.DEDICATED_DB && !context.tenantLineEnabled()) {
            return true;
        }
        Set<String> ignoreTables = normalize(options.ignoreTables());
        return ignoreTables.contains(tableName == null ? "" : tableName.toLowerCase());
    }

    private Set<String> normalize(String[] tables) {
        if (tables == null || tables.length == 0) {
            return Set.of();
        }
        Set<String> items = new HashSet<>();
        Arrays.stream(tables)
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toLowerCase())
                .forEach(items::add);
        return items;
    }
}
