package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.ObjectReadArguments;
import io.minio.ObjectReadArgs;

/**
 * <p>统一定义对象读取请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:58
 */
public abstract class ArgumentsToObjectReadConverter<S extends ObjectReadArguments, T extends ObjectReadArgs, B extends ObjectReadArgs.Builder<B, T>> extends ArgumentsToObjectVersionConverter<S, T, B> {

}
