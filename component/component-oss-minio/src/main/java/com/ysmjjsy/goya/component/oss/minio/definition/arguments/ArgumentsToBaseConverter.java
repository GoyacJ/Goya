package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.BaseArguments;
import io.minio.BaseArgs;
import org.apache.commons.collections4.MapUtils;

/**
 * <p>基础的统一定义请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:55
 */
public abstract class ArgumentsToBaseConverter<S extends BaseArguments, T extends BaseArgs, B extends BaseArgs.Builder<B, T>> implements ArgumentsConverter<S, T, B> {

    @Override
    public void prepare(S arguments, B builder) {
        if (MapUtils.isNotEmpty(arguments.getExtraHeaders())) {
            builder.extraHeaders(arguments.getExtraHeaders());
        }

        if (MapUtils.isNotEmpty(arguments.getExtraQueryParams())) {
            builder.extraQueryParams(arguments.getExtraQueryParams());
        }
    }
}