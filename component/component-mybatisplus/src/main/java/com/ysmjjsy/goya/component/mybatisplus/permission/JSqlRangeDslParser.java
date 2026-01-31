package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import com.ysmjjsy.goya.component.framework.security.dsl.BetweenPredicate;
import com.ysmjjsy.goya.component.framework.security.dsl.BinaryExpression;
import com.ysmjjsy.goya.component.framework.security.dsl.BooleanValue;
import com.ysmjjsy.goya.component.framework.security.dsl.ComparisonOperator;
import com.ysmjjsy.goya.component.framework.security.dsl.ComparisonPredicate;
import com.ysmjjsy.goya.component.framework.security.dsl.DateTimeValue;
import com.ysmjjsy.goya.component.framework.security.dsl.InPredicate;
import com.ysmjjsy.goya.component.framework.security.dsl.LogicalOperator;
import com.ysmjjsy.goya.component.framework.security.dsl.NotExpression;
import com.ysmjjsy.goya.component.framework.security.dsl.NullPredicate;
import com.ysmjjsy.goya.component.framework.security.dsl.NullValue;
import com.ysmjjsy.goya.component.framework.security.dsl.NumberValue;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeDslParser;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeExpression;
import com.ysmjjsy.goya.component.framework.security.dsl.StringValue;
import com.ysmjjsy.goya.component.framework.security.dsl.Value;
import tools.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>基于 JSON 的 DSL 解析器。</p>
 *
 * @author goya
 * @since 2026/1/31 11:10
 */
public class JSqlRangeDslParser implements RangeDslParser {

    /**
     * 解析 DSL 为范围表达式。
     *
     * @param dsl DSL 字符串
     * @return 范围表达式
     */
    @Override
    public RangeExpression parse(String dsl) {
        if (StringUtils.isBlank(dsl)) {
            throw new IllegalArgumentException("DSL 不能为空");
        }
        JsonNode root = GoyaJson.toJsonNode(dsl);
        if (root == null || root.isNull()) {
            throw new IllegalArgumentException("DSL 必须为 JSON 结构");
        }
        RangeExpression expression = parseExpression(root);
        if (expression == null) {
            throw new IllegalArgumentException("DSL 解析失败");
        }
        return expression;
    }

    private RangeExpression parseExpression(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        
        String type = GoyaJson.findStringValue(node, "type");
        String operator = GoyaJson.findStringValue(node, "operator");
        if (!StringUtils.isNotBlank(type)) {
            type = GoyaJson.findStringValue(node, "op");
        }
        String marker = StringUtils.isNotBlank(type) ? type : operator;
        if (StringUtils.isBlank(marker)) {
            if (node.has("left") && node.has("right")) {
                marker = "AND";
            } else if (node.has("field") && node.has("value")) {
                marker = "COMPARE";
            }
        }
        marker = marker == null ? "" : marker.trim().toUpperCase();

        switch (marker) {
            case "AND", "OR" -> {
                BinaryExpression expr = new BinaryExpression();
                expr.setOperator("AND" .equals(marker) ? LogicalOperator.AND : LogicalOperator.OR);
                expr.setLeft(parseExpression(node.get("left")));
                expr.setRight(parseExpression(node.get("right")));
                return expr;
            }
            case "NOT" -> {
                NotExpression notExpression = new NotExpression();
                JsonNode inner = node.has("expression") ? node.get("expression") : node.get("expr");
                notExpression.setExpression(parseExpression(inner));
                return notExpression;
            }
            case "BETWEEN" -> {
                BetweenPredicate predicate = new BetweenPredicate();
                predicate.setField(GoyaJson.findStringValue(node, "field"));
                predicate.setStart(parseValue(node.get("start")));
                predicate.setEnd(parseValue(node.get("end")));
                return predicate;
            }
            case "IN" -> {
                InPredicate predicate = new InPredicate();
                predicate.setField(GoyaJson.findStringValue(node, "field"));
                predicate.setNegated(node.path("negated").asBoolean(false) || node.path("not").asBoolean(false));
                predicate.setValues(parseValues(node.get("values")));
                return predicate;
            }
            case "NULL", "IS_NULL" -> {
                NullPredicate predicate = new NullPredicate();
                predicate.setField(GoyaJson.findStringValue(node, "field"));
                predicate.setNegated(node.path("negated").asBoolean(false) || node.path("not").asBoolean(false));
                return predicate;
            }
            case "COMPARE", "COMPARISON" -> {
                ComparisonPredicate predicate = new ComparisonPredicate();
                predicate.setField(GoyaJson.findStringValue(node, "field"));
                predicate.setOperator(parseComparisonOperator(GoyaJson.findStringValue(node, "operator")));
                if (predicate.getOperator() == null) {
                    predicate.setOperator(parseComparisonOperator(GoyaJson.findStringValue(node, "op")));
                }
                predicate.setValue(parseValue(node.get("value")));
                return predicate;
            }
            default -> {
                if (node.has("field") && node.has("value")) {
                    ComparisonPredicate predicate = new ComparisonPredicate();
                    predicate.setField(GoyaJson.findStringValue(node, "field"));
                    String op = GoyaJson.findStringValue(node, "operator");
                    if (!StringUtils.isNotBlank(op)) {
                        op = GoyaJson.findStringValue(node, "op");
                    }
                    if (!StringUtils.isNotBlank(op)) {
                        op = marker;
                    }
                    predicate.setOperator(parseComparisonOperator(op));
                    predicate.setValue(parseValue(node.get("value")));
                    return predicate;
                }
            }
        }
        return null;
    }

    private ComparisonOperator parseComparisonOperator(String operator) {
        if (!StringUtils.isNotBlank(operator)) {
            return null;
        }
        String normalized = operator.trim().toUpperCase();
        return switch (normalized) {
            case "EQ", "=" -> ComparisonOperator.EQ;
            case "NE", "!=" -> ComparisonOperator.NE;
            case "GT", ">" -> ComparisonOperator.GT;
            case "GTE", ">=" -> ComparisonOperator.GTE;
            case "LT", "<" -> ComparisonOperator.LT;
            case "LTE", "<=" -> ComparisonOperator.LTE;
            case "LIKE" -> ComparisonOperator.LIKE;
            default -> null;
        };
    }

    private List<Value> parseValues(JsonNode node) {
        if (node == null || !node.isArray()) {
            return List.of();
        }
        List<Value> values = new ArrayList<>();
        for (JsonNode item : node) {
            Value value = parseValue(item);
            if (value != null) {
                values.add(value);
            }
        }
        return values;
    }

    private Value parseValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return new NullValue();
        }
        if (node.isObject()) {
            String type = GoyaJson.findStringValue(node, "type");
            JsonNode valueNode = node.has("value") ? node.get("value") : null;
            if (StringUtils.isNotBlank(type)) {
                return parseTypedValue(type, valueNode);
            }
            if (valueNode != null) {
                return parseValue(valueNode);
            }
        }
        if (node.isString()) {
            StringValue value = new StringValue();
            value.setValue(node.asString());
            return value;
        }
        if (node.isNumber()) {
            NumberValue value = new NumberValue();
            value.setValue(node.numberValue());
            return value;
        }
        if (node.isBoolean()) {
            BooleanValue value = new BooleanValue();
            value.setValue(node.asBoolean());
            return value;
        }
        return null;
    }

    private Value parseTypedValue(String type, JsonNode node) {
        String normalized = type.trim().toUpperCase();
        switch (normalized) {
            case "STRING" -> {
                StringValue value = new StringValue();
                value.setValue(node == null || node.isNull() ? null : node.asString());
                return value;
            }
            case "NUMBER" -> {
                NumberValue value = new NumberValue();
                if (node != null && node.isNumber()) {
                    value.setValue(node.numberValue());
                }
                return value;
            }
            case "BOOLEAN" -> {
                BooleanValue value = new BooleanValue();
                if (node != null && node.isBoolean()) {
                    value.setValue(node.asBoolean());
                }
                return value;
            }
            case "DATETIME", "TIMESTAMP" -> {
                DateTimeValue value = new DateTimeValue();
                if (node != null) {
                    if (node.isString()) {
                        value.setValue(LocalDateTime.parse(node.asString()));
                    } else if (node.isNumber()) {
                        Instant instant = Instant.ofEpochMilli(node.asLong());
                        value.setValue(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                    }
                }
                return value;
            }
            case "NULL" -> {
                return new NullValue();
            }
            default -> {
                return parseValue(node);
            }
        }
    }
}
