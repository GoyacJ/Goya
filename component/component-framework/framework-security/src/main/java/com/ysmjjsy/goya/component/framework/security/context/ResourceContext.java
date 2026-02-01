package com.ysmjjsy.goya.component.framework.security.context;

import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * <p>资源解析上下文。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class ResourceContext implements Serializable {
    @Serial
    private static final long serialVersionUID = 6276245558701101738L;

    /**
     * 资源编码
     */
    @NotBlank
    @Schema(description = "资源编码")
    private String resourceCode;

    /**
     * 资源类型
     */
    @NotNull
    @Schema(description = "资源类型")
    private ResourceType resourceType;

    /**
     * 额外信息
     */
    @Schema(description = "额外信息")
    private Map<String, Object> attributes;
}
