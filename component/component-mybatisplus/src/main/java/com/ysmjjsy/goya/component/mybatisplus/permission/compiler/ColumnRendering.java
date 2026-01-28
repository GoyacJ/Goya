package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import com.ysmjjsy.goya.component.mybatisplus.exception.CrossTableReferenceException;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ColumnRef;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.Locale;
import java.util.Objects;

/**
 * <p>列渲染工具：严格禁止跨表引用版本</p>
 *
 * @author goya
 * @since 2026/1/28 23:16
 */
final class ColumnRendering {

    private ColumnRendering() {
    }

    /**
     * 构造列引用（严格模式：禁止跨表）。
     * <p>
     * 规则：
     * <ul>
     *   <li>ref.table 为空：允许，使用当前 alias（若存在）</li>
     *   <li>ref.table 非空：必须与 current.name 匹配，否则抛异常</li>
     *   <li>匹配时：优先用 current.alias，否则用 current.name/ref.table</li>
     * </ul>
     *
     * @param ref 列引用（来自白名单）
     * @param current 当前回调的 Table（来自 MP）
     * @return Column
     */
    static Column buildColumn(ColumnRef ref, Table current) {
        Objects.requireNonNull(ref, "ColumnRef 不能为空");

        String currentName = current == null ? null : current.getName();
        String currentAlias = null;
        if (current != null && current.getAlias() != null) {
            currentAlias = current.getAlias().getName();
        }

        // 1) ref.table 为空：作用于当前表
        if (ref.table() == null || ref.table().isBlank()) {
            if (currentAlias != null && !currentAlias.isBlank()) {
                return new Column(new Table(currentAlias), ref.column());
            }
            // 无 alias：只能用裸列名（可能歧义，但这是 SQL 本身的问题）
            return new Column(ref.column());
        }

        // 2) ref.table 非空：必须与 current.name 匹配（严格禁止跨表）
        if (currentName == null || currentName.isBlank()) {
            throw new CrossTableReferenceException("当前 SQL 表名缺失，无法校验字段归属：ref.table=" + ref.table());
        }

        String a = normalize(ref.table());
        String b = normalize(currentName);
        if (!a.equals(b)) {
            throw new CrossTableReferenceException(
                    "禁止跨表字段引用：ref.table=" + ref.table() + " current.table=" + currentName
            );
        }

        // 3) 匹配：优先 alias，其次用 currentName/ref.table
        if (currentAlias != null && !currentAlias.isBlank()) {
            return new Column(new Table(currentAlias), ref.column());
        }
        return new Column(new Table(currentName), ref.column());
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }
}