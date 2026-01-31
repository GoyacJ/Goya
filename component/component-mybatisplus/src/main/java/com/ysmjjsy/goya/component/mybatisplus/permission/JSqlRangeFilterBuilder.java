package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.google.common.collect.Sets;
import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.security.dsl.*;
import com.ysmjjsy.goya.component.framework.security.dsl.ComparisonOperator;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import org.apache.commons.collections4.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;

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
        if (expression != null && sqlExpression == null) {
            throw new IllegalArgumentException("DSL 表达式不支持或解析失败");
        }
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
                throw new IllegalArgumentException("禁止直接传入 SQL DSL");
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
            case ComparisonPredicate comparisonPredicate -> {
                return toComparisonExpression(comparisonPredicate);
            }
            case BetweenPredicate betweenPredicate -> {
                return toBetweenExpression(betweenPredicate);
            }
            case InPredicate inPredicate -> {
                return toInExpression(inPredicate);
            }
            case NullPredicate nullPredicate -> {
                return toNullExpression(nullPredicate);
            }
            default -> {
            }
        }
        return null;
    }

    private Expression toComparisonExpression(ComparisonPredicate predicate) {
        if (predicate == null || predicate.getOperator() == null || predicate.getField() == null) {
            return null;
        }
        Column column = new Column(predicate.getField());
        Expression value = toValueExpression(predicate.getValue());
        ComparisonOperator operator = predicate.getOperator();
        return switch (operator) {
            case EQ -> new EqualsTo(column, value);
            case NE -> new NotEqualsTo(column, value);
            case GT -> new GreaterThan(column, value);
            case GTE -> new GreaterThanEquals(column, value);
            case LT -> new MinorThan(column, value);
            case LTE -> new MinorThanEquals(column, value);
            case LIKE -> {
                LikeExpression likeExpression = new LikeExpression();
                likeExpression.setLeftExpression(column);
                likeExpression.setRightExpression(value);
                yield likeExpression;
            }
        };
    }

    private Expression toBetweenExpression(BetweenPredicate predicate) {
        if (predicate == null || predicate.getField() == null) {
            return null;
        }
        Between between = new Between();
        between.setLeftExpression(new Column(predicate.getField()));
        between.setBetweenExpressionStart(toValueExpression(predicate.getStart()));
        between.setBetweenExpressionEnd(toValueExpression(predicate.getEnd()));
        return between;
    }

    private Expression toInExpression(InPredicate predicate) {
        if (predicate == null || predicate.getField() == null) {
            return null;
        }
        InExpression inExpression = new InExpression();
        inExpression.setLeftExpression(new Column(predicate.getField()));
        List<Expression> expressions = new ArrayList<>();
        if (predicate.getValues() != null) {
            for (Value value : predicate.getValues()) {
                expressions.add(toValueExpression(value));
            }
        }
        inExpression.setRightExpression(new ExpressionList<>(expressions));
        inExpression.setNot(predicate.isNegated());
        return inExpression;
    }

    private Expression toNullExpression(NullPredicate predicate) {
        if (predicate == null || predicate.getField() == null) {
            return null;
        }
        IsNullExpression expression = new IsNullExpression();
        expression.setLeftExpression(new Column(predicate.getField()));
        expression.setNot(predicate.isNegated());
        return expression;
    }

    private Expression toValueExpression(Value value) {
        switch (value) {
            case null -> {
                return new net.sf.jsqlparser.expression.NullValue();
            }
            case NullValue _ -> {
                return new net.sf.jsqlparser.expression.NullValue();
            }
            case StringValue stringValue -> {
                return new net.sf.jsqlparser.expression.StringValue(stringValue.getValue());
            }
            case NumberValue numberValue -> {
                Number number = numberValue.getValue();
                if (number == null) {
                    return new net.sf.jsqlparser.expression.NullValue();
                }
                if (number instanceof java.math.BigDecimal) {
                    return new net.sf.jsqlparser.expression.DoubleValue(number.toString());
                }
                if (number instanceof Float || number instanceof Double) {
                    return new net.sf.jsqlparser.expression.DoubleValue(number.doubleValue());
                }
                if (number instanceof java.math.BigInteger) {
                    return new LongValue(number.toString());
                }
                return new LongValue(number.longValue());
            }
            case BooleanValue booleanValue -> {
                Boolean val = booleanValue.getValue();
                return val == null ? new net.sf.jsqlparser.expression.NullValue() : new net.sf.jsqlparser.expression.BooleanValue(val);
            }
            case DateTimeValue dateTimeValue -> {
                if (dateTimeValue.getValue() == null) {
                    return new net.sf.jsqlparser.expression.NullValue();
                }
                return new net.sf.jsqlparser.expression.TimestampValue(dateTimeValue.getValue().format(DateTimeFormatter.ofPattern(DefaultConst.DATE_FORMAT_YYYY_MM_DD_HH_MM_SS)));
            }
            default -> {
            }
        }
        return new net.sf.jsqlparser.expression.NullValue();
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
