package com.ysmjjsy.goya.component.framework.oss.arguments.base;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>基础的对象请求参数定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectArguments extends BucketArguments {

    @Serial
    private static final long serialVersionUID = -5919770690889639823L;

    @NotBlank(message = "对象名称不能为空")
    @Schema(name = "对象名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectName;
}
