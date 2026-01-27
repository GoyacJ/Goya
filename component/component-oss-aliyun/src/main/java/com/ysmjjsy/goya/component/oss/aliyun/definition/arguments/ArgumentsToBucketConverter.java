package com.ysmjjsy.goya.component.oss.aliyun.definition.arguments;

import com.aliyun.oss.model.WebServiceRequest;
import com.ysmjjsy.goya.component.framework.oss.arguments.base.BucketArguments;

/**
 * <p>统一定义存储桶请求参数转换为 Aliyun 参数转换器 </p>
 *
 * @author goya
 * @since 2023/8/10 15:37
 */
public abstract class ArgumentsToBucketConverter<S extends BucketArguments, T extends WebServiceRequest> extends ArgumentsToBaseConverter<S, T> {

}
