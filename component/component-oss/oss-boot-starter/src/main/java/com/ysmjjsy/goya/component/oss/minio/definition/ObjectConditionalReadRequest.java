package com.ysmjjsy.goya.component.oss.minio.definition;

import com.ysmjjsy.goya.component.core.utils.GoyaDateUtils;
import io.minio.ObjectConditionalReadArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

/**
 * <p> ObjectConditionalReadRequest </p>
 *
 * @author goya
 * @since 2023/5/30 23:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ObjectConditionalReadRequest<B extends ObjectConditionalReadArgs.Builder<B, A>, A extends ObjectConditionalReadArgs> extends ObjectReadRequest<B, A> {

    @Serial
    private static final long serialVersionUID = -5044967632002505979L;

    @Schema(name = "offset")
    @DecimalMin(value = "0", message = "offset 参数不能小于 0")
    private Long offset;

    @Schema(name = "length")
    @DecimalMin(value = "0", message = "length 参数不能小于 0")
    private Long length;

    @Schema(name = "matchETag")
    private String matchETag;

    @Schema(name = "notMatchETag")
    private String notMatchETag;
    private String modifiedSince;
    private String unmodifiedSince;

    @Override
    public void prepare(B builder) {

        if (ObjectUtils.isNotEmpty(getLength()) && getLength() >= 0) {
            builder.length(getLength());
        }

        if (ObjectUtils.isNotEmpty(getOffset()) && getOffset() >= 0) {
            builder.length(getOffset());
        }

        if (StringUtils.isNotBlank(getMatchETag())) {
            builder.matchETag(getMatchETag());
        }

        if (StringUtils.isNotBlank(getNotMatchETag())) {
            builder.matchETag(getNotMatchETag());
        }

        if (StringUtils.isNotBlank(getModifiedSince())) {
            builder.modifiedSince(GoyaDateUtils.stringToZonedDateTime(getModifiedSince()));
        }
        if (StringUtils.isNotBlank(getUnmodifiedSince())) {
            builder.unmodifiedSince(GoyaDateUtils.stringToZonedDateTime(getUnmodifiedSince()));
        }
        super.prepare(builder);
    }
}
