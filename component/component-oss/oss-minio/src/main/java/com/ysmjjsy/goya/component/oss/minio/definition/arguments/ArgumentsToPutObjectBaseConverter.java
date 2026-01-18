package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.base.PutObjectBaseArguments;
import io.minio.PutObjectBaseArgs;

/**
 * <p>统一定义对象写入基础请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:59
 */
public abstract class ArgumentsToPutObjectBaseConverter<S extends PutObjectBaseArguments, T extends PutObjectBaseArgs, B extends PutObjectBaseArgs.Builder<B, T>> extends ArgumentsToObjectWriteConverter<S, T, B> {
}
