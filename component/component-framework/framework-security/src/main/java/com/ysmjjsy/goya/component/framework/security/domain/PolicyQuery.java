package com.ysmjjsy.goya.component.framework.security.domain;

import com.ysmjjsy.goya.component.framework.common.pojo.DTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>策略查询条件。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyQuery implements DTO {
    @Serial
    private static final long serialVersionUID = -8683640379273116682L;

    /**
     * 租户编码
     */
    @Schema(description = "租户编码")
    private String tenantCode;

    /**
     * 主体信息
     */
    @NotNull
    @NonNull
    @Schema(description = "主体信息")
    private Subject subject;

    /**
     * 资源信息
     */
    @NotNull
    @NonNull
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
     * 授权策略信息
     */
    @Schema(description = "授权策略信息")
    private Map<String, Object> environment;
}
