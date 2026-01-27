package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import io.minio.DeleteBucketEncryptionArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 删除存储桶加密方式请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 22:05
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "删除存储桶加密方式请求参数实体", title = "删除存储桶加密方式请求参数实体")
public class DeleteBucketEncryptionRequest extends BucketRequest<DeleteBucketEncryptionArgs.Builder, DeleteBucketEncryptionArgs> {
    @Serial
    private static final long serialVersionUID = 5108570912805233153L;

    @Override
    public DeleteBucketEncryptionArgs.Builder getBuilder() {
        return DeleteBucketEncryptionArgs.builder();
    }
}
