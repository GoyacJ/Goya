package com.ysmjjsy.goya.component.framework.security.event;

import com.ysmjjsy.goya.component.framework.security.decision.DecisionType;
import com.ysmjjsy.goya.component.framework.security.domain.Action;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>鉴权审计事件</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class AuditEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = -7322529609530321010L;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 主体ID
     */
    @Schema(description = "主体ID")
    private String subjectId;

    /**
     * 主体类型
     */
    @Schema(description = "主体类型")
    private SubjectType subjectType;

    /**
     * 资源编码
     */
    @Schema(description = "资源编码")
    private String resourceCode;

    /**
     * 资源类型
     */
    @Schema(description = "资源类型")
    private ResourceType resourceType;

    /**
     * 行为动作
     */
    @Schema(description = "行为动作")
    private Action action;

    /**
     * 决策类型
     */
    @Schema(description = "决策类型")
    private DecisionType decisionType;

    /**
     * 发生时间
     */
    @Schema(description = "发生时间")
    private LocalDateTime occurredAt;

    /**
     * traceId
     */
    @Schema(description = "traceId")
    private String traceId;

    /**
     * 参数信息
     */
    @Schema(description = "参数信息")
    private Map<String, Object> attributes;
}
