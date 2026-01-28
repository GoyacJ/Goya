package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ColumnRef;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.schema.Table;

import java.util.List;
import java.util.Objects;

/**
 * <p>别名友好的已编译谓词实现</p>
 * <p>
 * 通过内部节点树（AST）延迟到渲染阶段再构造列引用：
 * <ul>
 *   <li>若当前 Table 存在别名，优先使用别名作为列前缀（例如 t.dept_id）</li>
 *   <li>若 ColumnRef.table 显式指定了表名：
 *       <ul>
 *         <li>当其与当前 Table.name 相同，则仍优先使用当前 alias</li>
 *         <li>当其不同，则使用 ColumnRef.table（表示规则明确指向别表列）</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <p><b>注意：</b>一般情况下建议 ColumnRef.table 为空或等于当前表，
 * 让权限条件只作用于当前回调的 Table，避免跨表条件导致不可预期行为。
 * @author goya
 * @since 2026/1/28 23:11
 */
public final class AliasAwareCompiledPredicate implements CompiledPredicate {

    private final Node root;
    private final Explain explain;

    public AliasAwareCompiledPredicate(Node root, Explain explain) {
        this.root = Objects.requireNonNull(root, "root 不能为空");
        this.explain = explain == null ? new Explain() : explain;
    }

    @Override
    public Expression toExpression(Table table) {
        return root.render(table);
    }

    /**
     * 获取编译解释信息（用于审计与排障）。
     *
     * @return Explain
     */
    public Explain explain() {
        return explain;
    }

    /**
     * 内部节点接口：延迟渲染为 JSqlParser Expression。
     */
    public interface Node {
        Expression render(Table table);
    }

    /**
     * AND 节点。
     */
    public record AndNode(Node left, Node right) implements Node {
        @Override
        public Expression render(Table table) {
            return new AndExpression(left.render(table), right.render(table));
        }
    }

    /**
     * OR 节点。
     */
    public record OrNode(Node left, Node right) implements Node {
        @Override
        public Expression render(Table table) {
            return new OrExpression(left.render(table), right.render(table));
        }
    }

    /**
     * EQ 节点。
     */
    public record EqNode(ColumnRef ref, Expression value) implements Node {
        @Override
        public Expression render(Table table) {
            EqualsTo eq = new EqualsTo();
            eq.setLeftExpression(ColumnRendering.buildColumn(ref, table));
            eq.setRightExpression(value);
            return eq;
        }
    }

    /**
     * IN 节点。
     */
    public record InNode(ColumnRef ref, List<Expression> values) implements Node {
        @Override
        public Expression render(Table table) {
            InExpression in = new InExpression();
            in.setLeftExpression(ColumnRendering.buildColumn(ref, table));
            in.setRightExpression(ExpressionListFactory.of(values));
            return in;
        }
    }

    /**
     * BETWEEN 节点。
     */
    public record BetweenNode(ColumnRef ref, Expression start, Expression end) implements Node {
        @Override
        public Expression render(Table table) {
            Between between = new Between();
            between.setLeftExpression(ColumnRendering.buildColumn(ref, table));
            between.setBetweenExpressionStart(start);
            between.setBetweenExpressionEnd(end);
            return between;
        }
    }

    /**
     * LIKE 节点。
     */
    public record LikeNode(ColumnRef ref, Expression pattern) implements Node {
        @Override
        public Expression render(Table table) {
            LikeExpression like = new LikeExpression();
            like.setLeftExpression(ColumnRendering.buildColumn(ref, table));
            like.setRightExpression(pattern);
            return like;
        }
    }
}