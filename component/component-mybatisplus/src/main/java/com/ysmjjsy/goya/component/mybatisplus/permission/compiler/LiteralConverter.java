package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import com.ysmjjsy.goya.component.mybatisplus.exception.PermissionCompileException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;

import java.math.BigDecimal;
import java.time.*;
import java.util.Date;
import java.util.List;

/**
 * <p>字面量转换器</p>
 * <p>
 * 负责将 Java 值转换为 JSqlParser 的 Literal Expression，并做必要的类型约束。
 *
 * <p><b>安全约束：</b>
 * <ul>
 *   <li>不允许将未知对象直接 toString 注入 SQL</li>
 *   <li>仅允许基础类型：String/Number/Boolean/Date/Temporal/Enum</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:49
 */
final class LiteralConverter {

    private LiteralConverter() {
    }

    static Expression toLiteral(Object v, Explain explain, String op) {
        if (v == null) {
            // 业务上通常不允许 null 参与比较；这里直接视为编译失败更安全
            explain.error(op + " 不允许 null 值");
            throw new PermissionCompileException("不允许 null 值", explain);
        }

        if (v instanceof String s) {
            return new StringValue(s);
        }
        if (v instanceof Boolean b) {
            // 不同数据库对布尔字面量差异较大；这里用 1/0 更通用
            return new LongValue(b ? 1 : 0);
        }
        if (v instanceof Integer i) {
            return new LongValue(i.longValue());
        }
        if (v instanceof Long l) {
            return new LongValue(l);
        }
        if (v instanceof Short s) {
            return new LongValue(s.longValue());
        }
        if (v instanceof Byte b) {
            return new LongValue(b.longValue());
        }
        if (v instanceof Float f) {
            return new DoubleValue(f.doubleValue());
        }
        if (v instanceof Double d) {
            return new DoubleValue(d);
        }
        if (v instanceof BigDecimal bd) {
            return new DoubleValue(bd.doubleValue());
        }
        if (v instanceof Enum<?> e) {
            // 枚举默认按 name 处理（可控且稳定）
            return new StringValue(e.name());
        }

        // 时间类型：统一转为字符串字面量（ISO-8601），由数据库端隐式/显式转换策略决定
        // 若你希望更严格，可按数据库方言输出 DateValue/TimestampValue
        if (v instanceof LocalDate ld) {
            return new StringValue(ld.toString());
        }
        if (v instanceof LocalDateTime ldt) {
            return new StringValue(ldt.toString());
        }
        if (v instanceof Instant ins) {
            return new StringValue(ins.toString());
        }
        if (v instanceof OffsetDateTime odt) {
            return new StringValue(odt.toString());
        }
        if (v instanceof ZonedDateTime zdt) {
            return new StringValue(zdt.toString());
        }
        if (v instanceof Date date) {
            return new StringValue(Instant.ofEpochMilli(date.getTime()).toString());
        }

        explain.error(op + " 不支持的值类型：" + v.getClass().getName());
        throw new PermissionCompileException("不支持的值类型", explain);
    }

    static ExpressionList toExpressionList(List<Object> values, Explain explain, String op) {
        ExpressionList list = new ExpressionList();
        for (Object v : values) {
            list.addExpressions(toLiteral(v, explain, op));
        }
        return list;
    }

    static boolean isBetweenCompatible(Object a, Object b) {
        if (a == null || b == null) {
            return false;
        }
        // 数值之间可比
        if (a instanceof Number && b instanceof Number) {
            return true;
        }
        // 字符串/日期时间之间：允许（由业务确保格式可比较；更严格可按类型分支）
        if ((a instanceof String || isTemporalLike(a)) && (b instanceof String || isTemporalLike(b))) {
            return true;
        }
        return false;
    }

    private static boolean isTemporalLike(Object v) {
        return v instanceof LocalDate
                || v instanceof LocalDateTime
                || v instanceof Instant
                || v instanceof OffsetDateTime
                || v instanceof ZonedDateTime
                || v instanceof Date;
    }
}
