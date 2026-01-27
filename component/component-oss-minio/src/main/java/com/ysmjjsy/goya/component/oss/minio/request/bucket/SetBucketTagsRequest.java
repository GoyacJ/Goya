package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import io.minio.SetBucketTagsArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serial;
import java.util.Map;

/**
 * <p> 设置存储桶标签请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 22:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "设置存储桶标签请求参数实体", title = "设置存储桶标签请求参数实体")
public class SetBucketTagsRequest extends BucketRequest<SetBucketTagsArgs.Builder, SetBucketTagsArgs> {

    @Serial
    private static final long serialVersionUID = -976304507181299640L;
    @Schema(name = "存储桶标签", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "存储桶标签不能为空")
    private Map<String, String> tags;

    @Override
    public void prepare(SetBucketTagsArgs.Builder builder) {
        if (ObjectUtils.isNotEmpty(getTags())) {
            builder.tags(getTags());
        }
        super.prepare(builder);
    }

    @Override
    public SetBucketTagsArgs.Builder getBuilder() {
        return SetBucketTagsArgs.builder();
    }
}
