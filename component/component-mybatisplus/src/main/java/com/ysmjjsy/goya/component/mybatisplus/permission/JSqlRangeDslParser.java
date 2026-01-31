package com.ysmjjsy.goya.component.mybatisplus.permission;

import com.ysmjjsy.goya.component.framework.security.dsl.RangeDslParser;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>基于 JSqlParser 的 DSL 解析器。</p>
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
        try {
            Expression expression = CCJSqlParserUtil.parseCondExpression(dsl);
            return new SqlRangeExpression(expression);
        } catch (Exception ex) {
            throw new IllegalArgumentException("DSL 解析失败", ex);
        }
    }
}
