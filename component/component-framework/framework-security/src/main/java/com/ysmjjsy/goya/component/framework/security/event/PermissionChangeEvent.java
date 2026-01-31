package com.ysmjjsy.goya.component.framework.security.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>权限变更事件</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class PermissionChangeEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = -2438226281075317966L;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 授权策略
     */
    @Schema(description = "授权策略")
    private String policyId;

    /**
     * 资源编码
     */
    @Schema(description = "资源编码")
    private String resourceCode;

    /**
     * 权限变更类型
     */
    @Schema(description = "权限变更类型")
    private PermissionChangeType changeType;

    /**
     * 变更时间
     */
    @Schema(description = "变更时间")
    private LocalDateTime changedAt;

    /**
     * 参数信息
     */
    @Schema(description = "参数信息")
    private Map<String, Object> attributes;
}
