package com.ysmjjsy.goya.component.oss.minio.definition.arguments;

import com.ysmjjsy.goya.component.framework.oss.arguments.base.BaseArguments;
import io.minio.BaseArgs;
import org.springframework.core.convert.converter.Converter;

/**
 * <p>统一定义请求参数转换为 Minio 参数转换器</p>
 *
 * @author goya
 * @since 2025/11/1 15:55
 */
public interface ArgumentsConverter<S extends BaseArguments, T extends BaseArgs, B extends BaseArgs.Builder<B, T>> extends Converter<S, T> {

    /**
     * 参数准备
     *
     * @param arguments 统一定义请求参数
     * @param builder   Minio 请求参数构造器
     */
    void prepare(S arguments, B builder);

    /**
     * 获取Minio 请求参数构造器
     *
     * @return Minio 请求参数构造器
     */
    B getBuilder();

    /**
     * 对象转换
     *
     * @param arguments 统一定义请求参数
     * @return Minio 请求参数
     */
    @Override
    default T convert(S arguments) {
        B builder = getBuilder();
        prepare(arguments, builder);
        return builder.build();
    }
}