package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectArguments;
import io.minio.ObjectArgs;

/**
 * <p>统一定义对象请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:57
 */
public abstract class ArgumentsToObjectConverter<S extends ObjectArguments, T extends ObjectArgs, B extends ObjectArgs.Builder<B, T>> extends ArgumentsToBucketConverter<S, T, B> {
    @Override
    public void prepare(S arguments, B builder) {
        builder.object(arguments.getObjectName());
        super.prepare(arguments, builder);
    }
}
