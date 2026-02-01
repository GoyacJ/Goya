package com.ysmjjsy.goya.component.framework.security.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * <p>解析后的资源。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource implements Serializable {

    @Serial
    private static final long serialVersionUID = -8485276852461917644L;

    /**
     * 资源编码
     */
    @NonNull
    @NotBlank
    @Schema(description = "资源编码")
    private String resourceCode;

    /**
     * 资源类型
     */
    @NotNull
    @NotBlank
    @Schema(description = "资源类型")
    private ResourceType resourceType;

    /**
     * 父资源编码
     */
    @Schema(description = "父资源编码")
    private String parentCode;

    /**
     * 父资源编码列表
     */
    @Schema(description = "父资源编码列表")
    private Set<String> parentCodes;

    /**
     * 资源负责人
     */
    @Schema(description = "资源负责人")
    private String resourceOwner;

    /**
     * 额外参数信息
     */
    @Schema(description = "额外参数信息")
    private Map<String, Object> attributes;
}
