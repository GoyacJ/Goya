package com.ysmjjsy.goya.component.oss.core.core.converter;

import org.springframework.core.convert.converter.Converter;

/**
 * <p>统一转换器定义</p>
 *
 * @author goya
 * @since 2025/11/1 14:22
 */
public interface OssConverter<S, T> extends Converter<S, T> {

    /**
     * 参数准备
     *
     * @param source   源对象
     * @param instance 转换后的对象
     */
    void prepare(S source, T instance);

    /**
     * 获取最终生成对象实例
     *
     * @param source 源对象。传递源对象，方便参数设置
     * @return 转换后的对象实例
     */
    T getInstance(S source);

    /**
     * 实体转换
     *
     * @param source 统一定义请求参数
     * @return 转换后的对象实例
     */
    @Override
    default T convert(S source) {
        T instance = getInstance(source);
        prepare(source, instance);
        return instance;
    }
}
