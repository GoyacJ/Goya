package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import io.minio.DeleteBucketTagsArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 删除存储桶标签请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 22:32
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "删除存储桶标签请求参数实体", title = "删除存储桶标签请求参数实体")
public class DeleteBucketTagsRequest extends BucketRequest<DeleteBucketTagsArgs.Builder, DeleteBucketTagsArgs> {

    @Serial
    private static final long serialVersionUID = 7681631893682758823L;

    @Override
    public DeleteBucketTagsArgs.Builder getBuilder() {
        return DeleteBucketTagsArgs.builder();
    }
}
