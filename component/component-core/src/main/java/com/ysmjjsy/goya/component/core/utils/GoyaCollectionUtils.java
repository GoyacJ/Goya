package com.ysmjjsy.goya.component.core.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 17:47
 */
@UtilityClass
public class GoyaCollectionUtils {

    public static boolean isEmpty(Object[] objects) {
        return ArrayUtils.isEmpty(objects);
    }

    public static boolean isNotEmpty(Object[] objects) {
        return !isEmpty(objects);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return CollectionUtils.isEmpty(collection);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] addAll(final T[] array1, final T... array2) {
        return ArrayUtils.addAll(array1, array2);
    }
}
