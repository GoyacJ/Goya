package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.google.common.collect.Sets;
import com.ysmjjsy.goya.component.framework.security.dsl.BinaryExpression;
import com.ysmjjsy.goya.component.framework.security.dsl.LogicalOperator;
import com.ysmjjsy.goya.component.framework.security.dsl.NotExpression;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeFilter;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeFilterBuilder;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeFilterContext;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.schema.Column;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>基于 JSqlParser 的范围过滤器构建器。</p>
 *
 * @author goya
 * @since 2026/1/31 11:10
 */
public class JSqlRangeFilterBuilder implements RangeFilterBuilder {

    /**
     * 将范围表达式构建为过滤器。
     *
     * @param expression 范围表达式
     * @param context    构建上下文
     * @return 范围过滤器
     */
    @Override
    public RangeFilter build(RangeExpression expression, RangeFilterContext context) {
        Expression sqlExpression = toSqlExpression(expression);
        validateColumns(sqlExpression, context);
        return sqlExpression == null ? null : new SqlRangeFilter(sqlExpression);
    }

    /**
     * 转换为sql 表达式
     *
     * @param expression 范围表达式
     * @return sql 表达式
     */
    private Expression toSqlExpression(RangeExpression expression) {
        switch (expression) {
            case null -> {
                return null;
            }
            case SqlRangeExpression sqlExpression -> {
                return sqlExpression.expression();
            }
            case BinaryExpression binaryExpression -> {
                Expression left = toSqlExpression(binaryExpression.getLeft());
                Expression right = toSqlExpression(binaryExpression.getRight());
                if (left == null || right == null) {
                    return null;
                }
                LogicalOperator operator = binaryExpression.getOperator();
                if (operator == LogicalOperator.AND) {
                    return new AndExpression(left, right);
                }
                if (operator == LogicalOperator.OR) {
                    return new OrExpression(left, right);
                }
                return null;
            }
            case NotExpression notExpression -> {
                Expression inner = toSqlExpression(notExpression.getExpression());
                return inner == null ? null : new net.sf.jsqlparser.expression.NotExpression(inner);
            }
            default -> {
            }
        }
        return null;
    }

    private void validateColumns(Expression expression, RangeFilterContext context) {
        if (expression == null || context == null || context.getResource() == null) {
            return;
        }
        Set<String> allowedColumns = extractAllowedColumns(context.getResource().getAttributes());
        if (CollectionUtils.isEmpty(allowedColumns)) {
            return;
        }
        List<String> invalidColumns = new ArrayList<>();
        expression.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Column column) {
                String name = column == null ? null : column.getColumnName();
                if (name != null && !allowedColumns.contains(name.toLowerCase())) {
                    invalidColumns.add(name);
                }
            }
        });
        if (!invalidColumns.isEmpty()) {
            throw new IllegalArgumentException("DSL 包含未授权字段: " + invalidColumns);
        }
    }

    private Set<String> extractAllowedColumns(Map<String, Object> attributes) {
        if (attributes == null) {
            return Sets.newHashSet();
        }
        Object value = attributes.get("allowedColumns");
        if (!(value instanceof Collection<?> collection)) {
            return Sets.newHashSet();
        }
        Set<String> allowed = new HashSet<>();
        for (Object item : collection) {
            if (item == null) {
                continue;
            }
            String text = item.toString().trim();
            if (!text.isEmpty()) {
                allowed.add(text.toLowerCase());
            }
        }
        return allowed;
    }
}
