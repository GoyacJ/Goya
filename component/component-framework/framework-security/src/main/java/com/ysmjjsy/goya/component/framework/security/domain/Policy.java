package com.ysmjjsy.goya.component.framework.security.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>授权策略定义</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class Policy implements Serializable {

    @Serial
    private static final long serialVersionUID = -8700766768451442246L;

    /**
     * 策略ID
     */
    @Schema(description = "策略ID")
    private String policyId;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 主体类型
     */
    @Schema(description = "主体类型")
    private SubjectType subjectType;

    /**
     * 主体ID
     */
    @Schema(description = "主体ID")
    private String subjectId;

    /**
     * 资源类型
     */
    @Schema(description = "资源类型")
    private ResourceType resourceType;

    /**
     * 资源编码
     */
    @Schema(description = "资源编码")
    private String resourceCode;

    /**
     * 行为动作
     */
    @Schema(description = "行为动作")
    private Action action;

    /**
     * 策略效果
     */
    @Schema(description = "策略效果")
    private PolicyEffect policyEffect;

    /**
     * 策略范围
     */
    @Schema(description = "策略范围")
    private PolicyScope policyScope;

    /**
     * 行级范围 DSL
     */
    @Schema(description = "行级范围 DSL")
    private String rangeDsl;

    /**
     * 列级允许名单
     */
    @Schema(description = "列级允许名单")
    private List<String> allowColumns;

    /**
     * 列级拒绝名单
     */
    @Schema(description = "列级拒绝名单")
    private List<String> denyColumns;

    /**
     * 显式继承标志
     */
    @Schema(description = "显式继承标志")
    private boolean inheritFlag;

    /**
     * 授权资源范围
     */
    @Schema(description = "授权资源范围")
    private ResourceRange resourceRange;

    /**
     * 永不过期
     */
    @Schema(description = "永不过期")
    private boolean neverExpire;

    /**
     * 过期时间
     */
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    /**
     * 额外信息
     */
    @Schema(description = "额外信息")
    private Map<String, Object> attributes;
}
