package com.ysmjjsy.goya.component.oss.aliyun.definition.arguments;

import com.aliyun.oss.model.WebServiceRequest;
import com.ysmjjsy.goya.component.framework.oss.arguments.base.ObjectArguments;

/**
 * <p>统一定义对象请求参数转换为 Aliyun 参数转换器 </p>
 *
 * @author goya
 * @since 2023/8/15 12:28
 */
public abstract class ArgumentsToObjectConverter<S extends ObjectArguments, T extends WebServiceRequest> extends ArgumentsToBucketConverter<S, T> {
}
