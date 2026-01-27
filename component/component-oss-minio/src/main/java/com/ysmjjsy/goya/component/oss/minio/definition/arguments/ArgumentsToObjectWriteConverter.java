package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.oss.core.arguments.base.ObjectWriteArguments;
import io.minio.ObjectWriteArgs;
import org.apache.commons.collections4.MapUtils;

/**
 * <p>统一定义对象写入请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:59
 */
public abstract class ArgumentsToObjectWriteConverter<S extends ObjectWriteArguments, T extends ObjectWriteArgs, B extends ObjectWriteArgs.Builder<B, T>> extends ArgumentsToObjectConverter<S, T, B> {

    @Override
    public void prepare(S arguments, B builder) {

        if (MapUtils.isNotEmpty(arguments.getRequestHeaders())) {
            builder.headers(arguments.getRequestHeaders());
        }

        if (MapUtils.isNotEmpty(arguments.getMetadata())) {
            builder.userMetadata(arguments.getMetadata());
        }

        super.prepare(arguments, builder);
    }
}
