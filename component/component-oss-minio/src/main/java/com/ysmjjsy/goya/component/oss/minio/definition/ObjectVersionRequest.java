package com.ysmjjsy.goya.component.oss.minio.definition;

import io.minio.ObjectVersionArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

/**
 * <p> Minio 基础 Object Version Request  </p>
 *
 * @author goya
 * @since 2023/4/18 14:16
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectVersionRequest<B extends ObjectVersionArgs.Builder<B, A>, A extends ObjectVersionArgs> extends ObjectRequest<B, A> {

    @Serial
    private static final long serialVersionUID = -8617752305860226966L;

    @Schema(name = "版本ID")
    private String versionId;

    @Override
    public void prepare(B builder) {
        if (StringUtils.isNotBlank(getVersionId())) {
            builder.versionId(getVersionId());
        }
        super.prepare(builder);
    }
}
