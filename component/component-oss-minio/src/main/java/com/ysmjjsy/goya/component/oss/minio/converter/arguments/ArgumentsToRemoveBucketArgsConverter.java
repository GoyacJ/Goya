package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.DeleteBucketArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToBucketConverter;
import io.minio.RemoveBucketArgs;

/**
 * <p>统一定义 DeleteBucketArguments 转 Minio RemoveBucketArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:14
 */
public class ArgumentsToRemoveBucketArgsConverter extends ArgumentsToBucketConverter<DeleteBucketArguments, RemoveBucketArgs, RemoveBucketArgs.Builder> {

    @Override
    public RemoveBucketArgs.Builder getBuilder() {
        return RemoveBucketArgs.builder();
    }
}
