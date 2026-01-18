package com.ysmjjsy.goya.component.oss.minio.definition;

import io.minio.ObjectArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> Minio 基础 Object Dto </p>
 *
 * @author goya
 * @since 2022/7/2 21:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectRequest<B extends ObjectArgs.Builder<B, A>, A extends ObjectArgs> extends BucketRequest<B, A> {

    @Serial
    private static final long serialVersionUID = -6977790802410331493L;

    @NotBlank(message = "对象名称不能为空")
    @Schema(name = "对象名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectName;

    @Override
    public void prepare(B builder) {
        builder.object(getObjectName());
        super.prepare(builder);
    }
}
