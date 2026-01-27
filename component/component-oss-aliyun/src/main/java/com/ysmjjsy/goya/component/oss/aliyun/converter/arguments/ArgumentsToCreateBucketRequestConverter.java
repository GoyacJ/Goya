package com.ysmjjsy.goya.component.oss.aliyun.converter.arguments;

import com.aliyun.oss.model.CreateBucketRequest;
import com.ysmjjsy.goya.component.framework.oss.arguments.bucket.CreateBucketArguments;
import com.ysmjjsy.goya.component.oss.aliyun.definition.arguments.ArgumentsToBucketConverter;

/**
 * <p>统一定义 CreateBucketArguments 转 Aliyun CreateBucketRequest 转换器 </p>
 *
 * @author goya
 * @since 2023/7/28 18:35
 */
public class ArgumentsToCreateBucketRequestConverter extends ArgumentsToBucketConverter<CreateBucketArguments, CreateBucketRequest> {

    @Override
    public CreateBucketRequest getInstance(CreateBucketArguments arguments) {
        return new CreateBucketRequest(arguments.getBucketName());
    }
}
