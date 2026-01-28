package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import com.ysmjjsy.goya.component.mybatisplus.exception.PermissionCompileException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

import java.util.List;

/**
 * <p>谓词表达式构造器</p>
 *
 * @author goya
 * @since 2026/1/28 22:48
 */
final class PredicateBuilders {

    private PredicateBuilders() {
    }

    static Expression eq(Column column, List<Object> values, Explain explain) {
        if (values.size() != 1) {
            explain.error("EQ 需要且仅需要 1 个值，实际=" + values.size());
            throw new PermissionCompileException("EQ 值数量错误", explain);
        }
        Object v = values.getFirst();
        EqualsTo eq = new EqualsTo();
        eq.setLeftExpression(column);
        eq.setRightExpression(LiteralConverter.toLiteral(v, explain, "EQ"));
        return eq;
    }

    static Expression in(Column column, List<Object> values, Explain explain) {
        if (values.isEmpty()) {
            explain.error("IN 值列表为空");
            throw new PermissionCompileException("IN 值列表为空", explain);
        }

        ExpressionList expressionList = LiteralConverter.toExpressionList(values, explain, "IN");
        InExpression in = new InExpression();
        in.setLeftExpression(column);
        in.setRightExpression(expressionList);
        return in;
    }

    static Expression between(Column column, List<Object> values, Explain explain) {
        if (values.size() != 2) {
            explain.error("BETWEEN 需要 2 个值，实际=" + values.size());
            throw new PermissionCompileException("BETWEEN 值数量错误", explain);
        }
        Object a = values.get(0);
        Object b = values.get(1);

        if (!LiteralConverter.isBetweenCompatible(a, b)) {
            explain.error("BETWEEN 两端类型不兼容：" + typeName(a) + " vs " + typeName(b));
            throw new PermissionCompileException("BETWEEN 类型不兼容", explain);
        }

        Between between = new Between();
        between.setLeftExpression(column);
        between.setBetweenExpressionStart(LiteralConverter.toLiteral(a, explain, "BETWEEN_START"));
        between.setBetweenExpressionEnd(LiteralConverter.toLiteral(b, explain, "BETWEEN_END"));
        return between;
    }

    static Expression like(Column column, List<Object> values, Explain explain) {
        if (values.size() != 1) {
            explain.error("LIKE 需要且仅需要 1 个值，实际=" + values.size());
            throw new PermissionCompileException("LIKE 值数量错误", explain);
        }
        Object v = values.get(0);
        if (!(v instanceof String s)) {
            explain.error("LIKE 仅支持字符串，实际=" + typeName(v));
            throw new PermissionCompileException("LIKE 类型错误", explain);
        }

        String pattern = s.trim();
        if (!isAllowedLikePattern(pattern)) {
            explain.error("LIKE 仅允许前缀或后缀匹配（abc% 或 %abc），禁止中缀匹配：" + pattern);
            throw new PermissionCompileException("LIKE 模式不安全", explain);
        }

        LikeExpression like = new LikeExpression();
        like.setLeftExpression(column);
        like.setRightExpression(new StringValue(pattern));
        return like;
    }

    /**
     * 仅允许：
     * <ul>
     *   <li>abc%（前缀匹配）</li>
     *   <li>%abc（后缀匹配）</li>
     *   <li>不含 % 的精确字符串（等价于不加通配，仍允许）</li>
     * </ul>
     * 禁止：%abc%（中缀匹配）以及多处 %。
     */
    private static boolean isAllowedLikePattern(String pattern) {
        if (pattern.isEmpty()) {
            return false;
        }
        int first = pattern.indexOf('%');
        if (first < 0) {
            return true;
        }
        int last = pattern.lastIndexOf('%');
        // 多个 % 或者中缀 %abc%：first==0 && last==len-1
        if (first != last) {
            return false;
        }
        if (first == 0 && last == pattern.length() - 1) {
            return false;
        }
        // 单个 %，且只能在首或尾
        return first == 0 || first == pattern.length() - 1;
    }

    private static String typeName(Object v) {
        return v == null ? "null" : v.getClass().getName();
    }
}