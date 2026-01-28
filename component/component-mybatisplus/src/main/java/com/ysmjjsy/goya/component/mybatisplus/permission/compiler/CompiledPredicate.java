package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

/**
 * <p>已编译的权限谓词</p>
 * <p>
 * 表示可直接用于 MyBatis-Plus DataPermission 的 SQL 条件表达式。
 *
 * @author goya
 * @since 2026/1/28 22:31
 */
public interface CompiledPredicate {

    /**
     * 生成权限表达式。
     * <p>
     *
     * @param table 当前处理的表信息（含别名）
     * @return Expression
     */
    Expression toExpression(Table table);
}
