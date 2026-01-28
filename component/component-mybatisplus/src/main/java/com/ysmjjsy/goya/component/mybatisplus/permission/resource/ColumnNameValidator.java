package com.ysmjjsy.goya.component.mybatisplus.permission.resource;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * <p>列名校验器</p>
 * <p>
 * 负责对 ColumnRef.column 进行强约束校验，防止通过列名注入 SQL 片段。
 *
 * <p><b>默认正则：</b>
 * 仅允许：字母/数字/下划线，并以字母或下划线开头。
 * 即：{@code ^[A-Za-z_][A-Za-z0-9_]*$}
 *
 * <p><b>注意：</b>
 * <ul>
 *   <li>不允许包含点号（.）、空格、引号、括号等任何可能形成表达式的字符</li>
 *   <li>如需支持带反引号/双引号的列名，请在业务侧统一规范化后再注册</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 23:07
 */
public class ColumnNameValidator {

    /**
     * 默认列名白名单正则。
     */
    public static final Pattern DEFAULT_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_]*$");

    private final Pattern pattern;

    public ColumnNameValidator() {
        this(DEFAULT_PATTERN);
    }

    public ColumnNameValidator(Pattern pattern) {
        this.pattern = Objects.requireNonNull(pattern, "pattern 不能为空");
    }

    /**
     * 校验列名是否合法。
     *
     * @param column 列名
     * @throws IllegalArgumentException 列名非法
     */
    public void validate(String column) {
        if (column == null || column.isBlank()) {
            throw new IllegalArgumentException("列名不能为空");
        }
        if (!pattern.matcher(column).matches()) {
            throw new IllegalArgumentException("列名不合法（不在白名单正则范围内）: " + column);
        }
    }

    /**
     * 校验表名是否合法（可选）。
     * <p>
     * 默认使用同列名规则，禁止 schema.table、禁止点号，避免注入。
     *
     * @param table 表名
     * @throws IllegalArgumentException 表名非法
     */
    public void validateTable(String table) {
        if (table == null || table.isBlank()) {
            return; // table 可选
        }
        if (!pattern.matcher(table).matches()) {
            throw new IllegalArgumentException("表名不合法（不在白名单正则范围内）: " + table);
        }
    }
}
