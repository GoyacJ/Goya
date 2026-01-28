package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>ExpressionList 工厂</p>
 * <p>
 * 目标：避免依赖某个具体版本的 API 形态（构造器/方法名可能不同）。
 *
 * <p>优先策略：</p>
 * <ol>
 *   <li>尝试 new ExpressionList(List&lt;Expression&gt;)</li>
 *   <li>否则 new ExpressionList() + setExpressions(List)</li>
 *   <li>否则 new ExpressionList() + addExpressions(Expression...) 或 addExpressions(Expression)</li>
 * </ol>
 * @author goya
 * @since 2026/1/28 23:12
 */
final class ExpressionListFactory {

    private ExpressionListFactory() {
    }

    static ExpressionList of(List<Expression> expressions) {
        List<Expression> safe = expressions == null ? List.of() : expressions;

        // 1) 构造器 ExpressionList(List)
        try {
            Constructor<ExpressionList> c = ExpressionList.class.getConstructor(List.class);
            return c.newInstance(new ArrayList<>(safe));
        } catch (Exception ignored) {
            // fallthrough
        }

        ExpressionList list;
        try {
            list = ExpressionList.class.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("无法创建 ExpressionList，请检查 jsqlparser 版本兼容性", e);
        }

        // 2) setExpressions(List)
        try {
            Method m = ExpressionList.class.getMethod("setExpressions", List.class);
            m.invoke(list, new ArrayList<>(safe));
            return list;
        } catch (Exception ignored) {
            // fallthrough
        }

        // 3) addExpressions
        // 3.1 addExpressions(Expression)
        try {
            Method m = ExpressionList.class.getMethod("addExpressions", Expression.class);
            for (Expression e : safe) {
                m.invoke(list, e);
            }
            return list;
        } catch (Exception ignored) {
            // fallthrough
        }

        // 3.2 addExpressions(Expression...)
        try {
            Method m = ExpressionList.class.getMethod("addExpressions", Expression[].class);
            m.invoke(list, (Object) safe.toArray(new Expression[0]));
            return list;
        } catch (Exception ignored) {
            // fallthrough
        }

        // 最后兜底：实在塞不进去就报错
        throw new IllegalStateException("ExpressionList 无法填充 expressions，请检查 jsqlparser 版本 API");
    }
}