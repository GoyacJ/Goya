package com.ysmjjsy.goya.component.common.mapstruct;

import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 00:08
 */
@FunctionalInterface
public interface IListConverter<S, T> extends Converter<List<S>, List<T>> {

    /**
     * 转换
     *
     * @param source List<S>
     * @return List<T>
     */
    @Override
    @Nullable
    default List<T> convert(List<S> source) {
        return source.stream()
                .map(this::from)
                .toList();
    }

    T from(S source);
}
