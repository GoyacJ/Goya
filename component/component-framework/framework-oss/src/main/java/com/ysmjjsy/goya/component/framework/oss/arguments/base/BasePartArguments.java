package com.ysmjjsy.goya.component.framework.oss.arguments.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>分片上传通用分片参数实体</p>
 *
 * @author goya
 * @since 2025/11/1 14:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class BasePartArguments extends ObjectArguments {

    @Serial
    private static final long serialVersionUID = 3849261134802884674L;

    /**
     * 分片上传ID
     */
    @Schema(name = "分片上传ID")
    @NotBlank(message = "分片上传ID为空")
    private String uploadId;
}
