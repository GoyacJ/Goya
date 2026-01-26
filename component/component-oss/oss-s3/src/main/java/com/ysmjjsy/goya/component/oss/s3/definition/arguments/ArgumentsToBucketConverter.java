package com.ysmjjsy.goya.component.oss.s3.definition.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.base.BucketArguments;
import software.amazon.awssdk.services.s3.model.S3Request;

/**
 * <p>统一定义存储桶请求参数转换为 S3 参数转换器</p>
 *
 * @author goya
 * @since 2023/8/10 15:37
 */
public abstract class ArgumentsToBucketConverter<S extends BucketArguments, T extends S3Request> extends ArgumentsToBaseConverter<S, T> {

}
