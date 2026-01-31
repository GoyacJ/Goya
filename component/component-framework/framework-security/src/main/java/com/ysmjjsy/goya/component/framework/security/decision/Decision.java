package com.ysmjjsy.goya.component.framework.security.decision;

import com.ysmjjsy.goya.component.framework.security.dsl.RangeFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>鉴权决策结果</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class Decision implements Serializable {
    @Serial
    private static final long serialVersionUID = -4501720271840538369L;

    /**
     * 决策类型
     */
    @Schema(description = "决策类型")
    private DecisionType decisionType;

    /**
     * 结果描述
     */
    @Schema(description = "结果描述")
    private String reason;

    /**
     * 范围过滤器
     */
    @Schema(description = "范围过滤器")
    private RangeFilter rowFilter;

    /**
     * 列级约束
     */
    @Schema(description = "列级约束")
    private ColumnConstraint columnConstraint;

    private boolean isAllow() {
        return DecisionType.ALLOW.equals(decisionType);
    }

    public static Decision deny(String reason) {
        Decision decision = new Decision();
        decision.setDecisionType(DecisionType.DENY);
        decision.setReason(reason);
        return decision;
    }
}
