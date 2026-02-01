package com.ysmjjsy.goya.component.framework.security.context;

import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * <p>主体解析上下文。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
public class SubjectContext implements Serializable {
    @Serial
    private static final long serialVersionUID = -7185645768705639909L;

    /**
     * 主体ID
     */
    @Schema(description = "主体ID")
    @NotBlank
    private String subjectId;

    /**
     * 主体类型
     */
    @NotNull
    @Schema(description = "主体类型")
    private SubjectType subjectType;

    /**
     * 额外信息
     */
    @Schema(description = "额外信息")
    private Map<String, Object> attributes;
}
