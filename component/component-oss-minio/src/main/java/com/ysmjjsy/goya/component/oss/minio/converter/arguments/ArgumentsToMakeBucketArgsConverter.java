package com.ysmjjsy.goya.component.oss.minio.converter.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.bucket.CreateBucketArguments;
import com.ysmjjsy.goya.component.oss.minio.definition.arguments.ArgumentsToBucketConverter;
import io.minio.MakeBucketArgs;
import org.apache.commons.lang3.ObjectUtils;

/**
 * <p>统一定义 CreateBucketArguments 转 Minio MakeBucketArgs 转换器</p>
 *
 * @author goya
 * @since 2025/11/1 16:14
 */
public class ArgumentsToMakeBucketArgsConverter extends ArgumentsToBucketConverter<CreateBucketArguments, MakeBucketArgs, MakeBucketArgs.Builder> {

    @Override
    public void prepare(CreateBucketArguments arguments, MakeBucketArgs.Builder builder) {
        if (ObjectUtils.isNotEmpty(arguments.getObjectLock())) {
            builder.objectLock(arguments.getObjectLock());
        }
        super.prepare(arguments, builder);
    }

    @Override
    public MakeBucketArgs.Builder getBuilder() {
        return MakeBucketArgs.builder();
    }
}
