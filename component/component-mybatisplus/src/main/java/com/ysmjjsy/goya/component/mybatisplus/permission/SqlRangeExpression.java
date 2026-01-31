package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.ysmjjsy.goya.component.framework.security.dsl.RangeExpression;
import net.sf.jsqlparser.expression.Expression;

/**
 * <p>SQL 条件表达式封装。</p>
 *
 * @author goya
 * @since 2026/1/31 11:10
 */
public record SqlRangeExpression(Expression expression) implements RangeExpression {

}
