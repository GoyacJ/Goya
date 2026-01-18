package com.ysmjjsy.goya.component.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 17:44
 */
@UtilityClass
public class GoyaMapUtils {

    public static boolean isEmpty(Map<?, ?> map) {
        return MapUtils.isEmpty(map);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <K, V, T extends Map<K, V>> T removeAny(T map, K... keys) {
        for(K key : keys) {
            map.remove(key);
        }

        return map;
    }
}
