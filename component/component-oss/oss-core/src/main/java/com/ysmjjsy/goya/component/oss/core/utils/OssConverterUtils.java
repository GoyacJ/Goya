package com.ysmjjsy.goya.component.oss.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.core.convert.converter.Converter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>实体转换工具类</p>
 *
 * @author goya
 * @since 2025/11/1 17:22
 */
@UtilityClass
public class OssConverterUtils {
    public static <T, R> List<R> toDomains(List<T> items, Converter<T, R> toDomain) {
        if (CollectionUtils.isNotEmpty(items)) {
            return items.stream().map(toDomain::convert).toList();
        }
        return new ArrayList<>();
    }

    public static <T, R> R toDomain(T object, Converter<T, R> toDomain) {
        return toDomain.convert(object);
    }
}