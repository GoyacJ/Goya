package com.ysmjjsy.goya.component.oss.minio.request.bucket;

import com.ysmjjsy.goya.component.oss.minio.definition.BucketRequest;
import io.minio.DeleteObjectLockConfigurationArgs;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p> 删除存储桶对象锁定配置请求参数实体 </p>
 *
 * @author goya
 * @since 2023/6/6 23:01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "删除存储桶对象锁定配置请求参数实体", title = "删除存储桶对象锁定配置请求参数实体")
public class DeleteObjectLockConfigurationRequest extends BucketRequest<DeleteObjectLockConfigurationArgs.Builder, DeleteObjectLockConfigurationArgs> {
    @Serial
    private static final long serialVersionUID = -5414442097478698225L;

    @Override
    public DeleteObjectLockConfigurationArgs.Builder getBuilder() {
        return DeleteObjectLockConfigurationArgs.builder();
    }
}
