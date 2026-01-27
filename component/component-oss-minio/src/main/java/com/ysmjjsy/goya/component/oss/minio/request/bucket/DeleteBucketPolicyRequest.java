package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import io.minio.DeleteBucketPolicyArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 删除存储桶访问策略请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 21:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "删除存储桶访问策略请求参数实体", title = "删除存储桶访问策略请求参数实体")
public class DeleteBucketPolicyRequest extends BucketRequest<DeleteBucketPolicyArgs.Builder, DeleteBucketPolicyArgs> {
    @Serial
    private static final long serialVersionUID = 8751837694227722002L;

    @Override
    public DeleteBucketPolicyArgs.Builder getBuilder() {
        return DeleteBucketPolicyArgs.builder();
    }
}
