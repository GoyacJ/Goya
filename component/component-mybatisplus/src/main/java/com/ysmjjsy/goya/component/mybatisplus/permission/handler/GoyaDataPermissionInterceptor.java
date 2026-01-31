package com.ysmjjsy.goya.component.mybatisplus.permission.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.ysmjjsy.goya.component.framework.security.decision.ColumnConstraint;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.permission.PermissionContextHolder;
import lombok.EqualsAndHashCode;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * <p>数据权限拦截器</p>
 *
 * <p>默认仅对查询生效，写入权限由业务侧控制。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@EqualsAndHashCode(callSuper = true)
public class GoyaDataPermissionInterceptor extends DataPermissionInterceptor {

    private final boolean applyToWrite;
    private final boolean failClosed;

    private static final String ACTION_QUERY = "QUERY";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final String ACTION_DELETE = "DELETE";

    /**
     * 构造方法。
     *
     * @param handler 数据权限处理器
     * @param options 权限配置
     */
    public GoyaDataPermissionInterceptor(DataPermissionHandler handler, GoyaMybatisPlusProperties.Permission options) {
        super(handler);
        this.applyToWrite = options != null && options.applyToWrite();
        this.failClosed = options == null || options.failClosed();
    }

    /**
     * 查询语句处理。
     *
     * @param select 查询语句
     * @param index 索引
     * @param sql 原始 SQL
     * @param obj 参数
     */
    @Override
    protected void processSelect(Select select, int index, String sql, Object obj) {
        PermissionContextHolder.setAction(ACTION_QUERY);
        try {
            super.processSelect(select, index, sql, obj);
            applyColumnConstraint(select);
        } finally {
            PermissionContextHolder.clear();
        }
    }

    /**
     * 更新语句处理。
     *
     * @param update 更新语句
     * @param index 索引
     * @param sql 原始 SQL
     * @param obj 参数
     */
    @Override
    protected void processUpdate(net.sf.jsqlparser.statement.update.Update update, int index, String sql, Object obj) {
        if (applyToWrite) {
            PermissionContextHolder.setAction(ACTION_UPDATE);
            try {
                super.processUpdate(update, index, sql, obj);
            } finally {
                PermissionContextHolder.clear();
            }
        }
    }

    /**
     * 删除语句处理。
     *
     * @param delete 删除语句
     * @param index 索引
     * @param sql 原始 SQL
     * @param obj 参数
     */
    @Override
    protected void processDelete(net.sf.jsqlparser.statement.delete.Delete delete, int index, String sql, Object obj) {
        if (applyToWrite) {
            PermissionContextHolder.setAction(ACTION_DELETE);
            try {
                super.processDelete(delete, index, sql, obj);
            } finally {
                PermissionContextHolder.clear();
            }
        }
    }

    private void applyColumnConstraint(Select select) {
        Map<String, ColumnConstraint> raw = PermissionContextHolder.getConstraints();
        if (raw.isEmpty() || select == null) {
            return;
        }
        Map<String, NormalizedConstraint> constraints = normalizeConstraints(raw);
        if (constraints.isEmpty()) {
            return;
        }
        applySelect(select, constraints);
    }

    private void applySelect(Select select, Map<String, NormalizedConstraint> constraints) {
        if (select == null) {
            return;
        }
        List<WithItem<?>> withItems = select.getWithItemsList();
        if (!CollectionUtils.isEmpty(withItems)) {
            for (WithItem<?> withItem : withItems) {
                if (withItem != null && withItem.getSelect() != null) {
                    applySelect(withItem.getSelect(), constraints);
                }
            }
        }
        if (select instanceof ParenthesedSelect parenthesed) {
            applySelect(parenthesed.getSelect(), constraints);
            return;
        }
        if (select instanceof PlainSelect plainSelect) {
            applyPlainSelect(plainSelect, constraints);
            return;
        }
        if (select instanceof SetOperationList setOperationList) {
            List<Select> selects = setOperationList.getSelects();
            if (!CollectionUtils.isEmpty(selects)) {
                for (Select s : selects) {
                    applySelect(s, constraints);
                }
            }
        }
    }

    private void applyPlainSelect(PlainSelect plainSelect, Map<String, NormalizedConstraint> constraints) {
        if (plainSelect == null || CollectionUtils.isEmpty(plainSelect.getSelectItems())) {
            return;
        }
        List<TableRef> tableRefs = collectTableRefs(plainSelect, constraints);
        if (tableRefs.isEmpty()) {
            return;
        }
        boolean hasEffectiveConstraint = constraints.values().stream().anyMatch(NormalizedConstraint::effective);
        if (!hasEffectiveConstraint) {
            return;
        }

        List<SelectItem<?>> original = plainSelect.getSelectItems();
        List<SelectItem<?>> rebuilt = new ArrayList<>();
        boolean changed = false;
        boolean enforceFailed = false;

        for (SelectItem<?> item : original) {
            Expression expression = item == null ? null : item.getExpression();
            if (expression instanceof AllColumns) {
                List<SelectItem<?>> expanded = expandAllColumns(item, tableRefs, constraints);
                if (expanded == null) {
                    enforceFailed = true;
                    break;
                }
                rebuilt.addAll(expanded);
                changed = true;
                continue;
            }
            if (expression instanceof AllTableColumns allTableColumns) {
                List<SelectItem<?>> expanded = expandAllTableColumns(item, allTableColumns, tableRefs, constraints);
                if (expanded == null) {
                    enforceFailed = true;
                    break;
                }
                rebuilt.addAll(expanded);
                changed = changed || expanded.size() != 1 || expanded.get(0) != item;
                continue;
            }
            if (expression == null) {
                rebuilt.add(item);
                continue;
            }
            if (violatesConstraint(expression, tableRefs, constraints)) {
                changed = true;
                continue;
            }
            rebuilt.add(item);
        }

        if (enforceFailed || rebuilt.isEmpty()) {
            if (failClosed) {
                denySelect(plainSelect);
            }
            return;
        }
        if (changed) {
            plainSelect.setSelectItems(rebuilt);
        }
    }

    private boolean violatesConstraint(Expression expression,
                                       List<TableRef> tableRefs,
                                       Map<String, NormalizedConstraint> constraints) {
        List<Column> columns = new ArrayList<>();
        expression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Column column) {
                if (column != null) {
                    columns.add(column);
                }
            }
        });
        if (columns.isEmpty()) {
            return false;
        }
        for (Column column : columns) {
            if (!isColumnAllowed(column, tableRefs, constraints)) {
                return true;
            }
        }
        return false;
    }

    private boolean isColumnAllowed(Column column,
                                    List<TableRef> tableRefs,
                                    Map<String, NormalizedConstraint> constraints) {
        String columnName = column.getColumnName();
        if (!StringUtils.hasText(columnName)) {
            return true;
        }
        String tableToken = null;
        if (column.getTable() != null && StringUtils.hasText(column.getTable().getName())) {
            tableToken = column.getTable().getName();
        }
        TableRef ref = resolveTableRef(tableToken, tableRefs);
        if (ref == null) {
            return !hasEffectiveConstraint(constraints);
        }
        NormalizedConstraint constraint = constraints.get(ref.tableNameLower());
        if (constraint == null || !constraint.effective()) {
            return true;
        }
        return constraint.allows(columnName);
    }

    private boolean hasEffectiveConstraint(Map<String, NormalizedConstraint> constraints) {
        return constraints.values().stream().anyMatch(NormalizedConstraint::effective);
    }

    private List<SelectItem<?>> expandAllTableColumns(SelectItem<?> originalItem,
                                                      AllTableColumns allTableColumns,
                                                      List<TableRef> tableRefs,
                                                      Map<String, NormalizedConstraint> constraints) {
        Table table = allTableColumns.getTable();
        String tableToken = table == null ? null : table.getName();
        TableRef ref = resolveTableRef(tableToken, tableRefs);
        if (ref == null) {
            return failClosed ? null : List.of(originalItem);
        }
        NormalizedConstraint constraint = constraints.get(ref.tableNameLower());
        if (constraint == null || !constraint.effective()) {
            return List.of(originalItem);
        }
        if (constraint.allow().isEmpty()) {
            return constraint.deny().isEmpty() ? List.of(originalItem) : (failClosed ? null : List.of(originalItem));
        }
        List<SelectItem<?>> items = new ArrayList<>();
        for (String col : constraint.allow()) {
            if (constraint.deny().contains(col)) {
                continue;
            }
            items.add(SelectItem.from(new Column(new Table(ref.qualifier()), col)));
        }
        return items;
    }

    private List<SelectItem<?>> expandAllColumns(SelectItem<?> originalItem,
                                                 List<TableRef> tableRefs,
                                                 Map<String, NormalizedConstraint> constraints) {
        List<SelectItem<?>> items = new ArrayList<>();
        for (TableRef ref : tableRefs) {
            NormalizedConstraint constraint = constraints.get(ref.tableNameLower());
            if (constraint == null || !constraint.effective()) {
                items.add(SelectItem.from(new AllTableColumns(new Table(ref.qualifier()))));
                continue;
            }
            if (constraint.allow().isEmpty()) {
                if (constraint.deny().isEmpty()) {
                    items.add(SelectItem.from(new AllTableColumns(new Table(ref.qualifier()))));
                    continue;
                }
                return failClosed ? null : List.of(originalItem);
            }
            for (String col : constraint.allow()) {
                if (constraint.deny().contains(col)) {
                    continue;
                }
                items.add(SelectItem.from(new Column(new Table(ref.qualifier()), col)));
            }
        }
        return items;
    }

    private TableRef resolveTableRef(String token, List<TableRef> tableRefs) {
        if (tableRefs.isEmpty()) {
            return null;
        }
        if (!StringUtils.hasText(token)) {
            return tableRefs.size() == 1 ? tableRefs.get(0) : null;
        }
        String normalized = token.trim().toLowerCase(Locale.ROOT);
        for (TableRef ref : tableRefs) {
            if (ref.aliasLower() != null && ref.aliasLower().equals(normalized)) {
                return ref;
            }
            if (ref.tableNameLower().equals(normalized)) {
                return ref;
            }
        }
        return tableRefs.size() == 1 ? tableRefs.get(0) : null;
    }

    private List<TableRef> collectTableRefs(PlainSelect plainSelect,
                                            Map<String, NormalizedConstraint> constraints) {
        List<TableRef> refs = new ArrayList<>();
        collectFromItem(plainSelect.getFromItem(), refs, constraints);
        List<Join> joins = plainSelect.getJoins();
        if (!CollectionUtils.isEmpty(joins)) {
            for (Join join : joins) {
                collectFromItem(join.getRightItem(), refs, constraints);
            }
        }
        return refs;
    }

    private void collectFromItem(FromItem item, List<TableRef> refs, Map<String, NormalizedConstraint> constraints) {
        if (item == null) {
            return;
        }
        if (item instanceof Table table) {
            String name = table.getName();
            if (!StringUtils.hasText(name)) {
                return;
            }
            String alias = table.getAlias() == null ? null : table.getAlias().getName();
            refs.add(new TableRef(name, alias));
            return;
        }
        if (item instanceof ParenthesedSelect parenthesedSelect) {
            applySelect(parenthesedSelect.getSelect(), constraints);
        }
    }

    private Map<String, NormalizedConstraint> normalizeConstraints(Map<String, ColumnConstraint> raw) {
        Map<String, NormalizedConstraint> normalized = new HashMap<>();
        for (Map.Entry<String, ColumnConstraint> entry : raw.entrySet()) {
            ColumnConstraint constraint = entry.getValue();
            if (constraint == null) {
                continue;
            }
            Set<String> allow = normalizeSet(constraint.getAllowColumns());
            Set<String> deny = normalizeSet(constraint.getDenyColumns());
            if (allow.isEmpty() && deny.isEmpty()) {
                continue;
            }
            normalized.put(entry.getKey().toLowerCase(Locale.ROOT), new NormalizedConstraint(allow, deny));
        }
        return normalized;
    }

    private Set<String> normalizeSet(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Set.of();
        }
        Set<String> normalized = new HashSet<>();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            normalized.add(value.trim().toLowerCase(Locale.ROOT));
        }
        return normalized;
    }

    private void denySelect(PlainSelect select) {
        Expression deny = denyExpression();
        if (deny == null) {
            return;
        }
        Expression where = select.getWhere();
        select.setWhere(where == null ? deny : new AndExpression(where, deny));
    }

    private Expression denyExpression() {
        try {
            return CCJSqlParserUtil.parseCondExpression("1 = 0");
        } catch (Exception _) {
            return null;
        }
    }

    private record TableRef(String tableName, String alias) {
        private String qualifier() {
            return StringUtils.hasText(alias) ? alias : tableName;
        }

        private String tableNameLower() {
            return tableName == null ? "" : tableName.toLowerCase(Locale.ROOT);
        }

        private String aliasLower() {
            return alias == null ? null : alias.toLowerCase(Locale.ROOT);
        }
    }

    private record NormalizedConstraint(Set<String> allow, Set<String> deny) {
        private boolean effective() {
            return !allow.isEmpty() || !deny.isEmpty();
        }

        private boolean allows(String column) {
            if (!StringUtils.hasText(column)) {
                return true;
            }
            String normalized = column.trim().toLowerCase(Locale.ROOT);
            if (!allow.isEmpty() && !allow.contains(normalized)) {
                return false;
            }
            return !deny.contains(normalized);
        }
    }
}
