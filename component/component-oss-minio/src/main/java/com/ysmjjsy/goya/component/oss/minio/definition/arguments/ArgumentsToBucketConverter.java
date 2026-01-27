package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.BucketArguments;
import io.minio.BucketArgs;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>统一定义存储桶请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:56
 */
public abstract class ArgumentsToBucketConverter<S extends BucketArguments, T extends BucketArgs, B extends BucketArgs.Builder<B, T>> extends ArgumentsToBaseConverter<S, T, B> {

    @Override
    public void prepare(S arguments, B builder) {

        builder.bucket(arguments.getBucketName());

        if (StringUtils.isNotBlank(arguments.getRegion())) {
            builder.region(arguments.getRegion());
        }

        super.prepare(arguments, builder);
    }
}
