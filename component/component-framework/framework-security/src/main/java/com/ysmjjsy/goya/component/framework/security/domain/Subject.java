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
import java.util.List;
import java.util.Map;

/**
 * <p>解析后的主体。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject implements Serializable {

    @Serial
    private static final long serialVersionUID = -7615465216061907711L;

    /**
     * 主体ID
     */
    @NonNull
    @NotBlank
    @Schema(description = "主体ID")
    private String subjectId;

    /**
     * 主体类型
     */
    @NonNull
    @NotNull
    @Schema(description = "主体类型")
    private SubjectType subjectType;

    /**
     * 所属角色 ID 列表。
     */
    @Schema(description = "所属角色 ID 列表")
    private List<String> roleIds;

    /**
     * 所属团队 ID 列表。
     */
    @Schema(description = "所属团队 ID 列表")
    private List<String> teamIds;

    /**
     * 所属组织/部门 ID 列表。
     */
    @Schema(description = "所属组织/部门 ID 列表")
    private List<String> orgIds;

    /**
     * 额外信息
     */
    @Schema(description = "额外信息")
    private Map<String, Object> attributes;
}
