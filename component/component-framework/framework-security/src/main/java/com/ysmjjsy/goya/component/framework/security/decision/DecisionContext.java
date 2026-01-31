package com.ysmjjsy.goya.component.framework.security.decision;

import com.ysmjjsy.goya.component.framework.security.domain.Action;
import com.ysmjjsy.goya.component.framework.security.domain.Policy;
import com.ysmjjsy.goya.component.framework.security.domain.Resource;
import com.ysmjjsy.goya.component.framework.security.domain.Subject;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>决策上下文</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 6864037071195784998L;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 主体信息
     */
    @Schema(description = "主体信息")
    private Subject subject;

    /**
     * 资源信息
     */
    @Schema(description = "资源信息")
    private Resource resource;

    /**
     * 行为动作
     */
    @NotNull
    @NonNull
    @Schema(description = "行为动作")
    private Action action;

    @NotNull
    @NonNull
    @Schema(description = "请求时间")
    private LocalDateTime requestTime;

    /**
     * 参数信息
     */
    @Schema(description = "参数信息")
    private Map<String, Object> environment;

    /**
     * 授权策略信息
     */
    @Schema(description = "授权策略信息")
    private List<Policy> policies;
}
