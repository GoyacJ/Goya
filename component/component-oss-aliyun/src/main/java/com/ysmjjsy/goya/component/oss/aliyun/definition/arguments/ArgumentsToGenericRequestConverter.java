package com.ysmjjsy.goya.component.oss.aliyun.definition.arguments;

import com.aliyun.oss.model.GenericRequest;
import com.ysmjjsy.goya.component.framework.oss.arguments.base.BucketArguments;

/**
 * <p>统一定义存储桶请求参数转换为 GenericRequest 参数转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 15:37
 */
public class ArgumentsToGenericRequestConverter<S extends BucketArguments> extends ArgumentsToBaseConverter<S, GenericRequest> {
    @Override
    public GenericRequest getInstance(S arguments) {
        return new GenericRequest(arguments.getBucketName());
    }
}
