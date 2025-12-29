package com.ysmjjsy.goya.component.common.utils;

import lombok.experimental.UtilityClass;

/**
 * <p>类型转换工具类</p>
 * <p>提供字符串到各种基本类型的转换功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 22:18
 */
@UtilityClass
public class ConvertUtils {

    // ==================== 字符串转换 ====================

    /**
     * 将对象转换为字符串
     *
     * @param value 对象值
     * @return 字符串，如果为 null 返回 null
     */
    public static String toStr(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    /**
     * 将对象转换为字符串（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 字符串，如果为 null 或空字符串则返回默认值
     */
    public static String toStr(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String str = value.toString().trim();
        return str.isEmpty() ? defaultValue : str;
    }

    // ==================== 整数转换 ====================

    /**
     * 将对象转换为整数
     *
     * @param value 对象值
     * @return 整数，如果转换失败返回 null
     */
    public static Integer toInt(Object value) {
        return toInt(value, null);
    }

    /**
     * 将对象转换为整数（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 整数，如果转换失败返回默认值
     */
    public static Integer toInt(Object value, Integer defaultValue) {
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number nu -> {
                return nu.intValue();
            }
            case Boolean bo -> {
                return bo ? 1 : 0;
            }
            default -> {
            }
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        try {
            // 处理小数点后的数字，如 "123.45" -> 123
            if (str.contains(".")) {
                return (int) Double.parseDouble(str);
            }
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 长整数转换 ====================

    /**
     * 将对象转换为长整数
     *
     * @param value 对象值
     * @return 长整数，如果转换失败返回 null
     */
    public static Long toLong(Object value) {
        return toLong(value, null);
    }

    /**
     * 将对象转换为长整数（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 长整数，如果转换失败返回默认值
     */
    public static Long toLong(Object value, Long defaultValue) {
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.longValue();
            }
            case Boolean b -> {
                return b ? 1L : 0L;
            }
            default -> {
            }
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        try {
            // 处理小数点后的数字，如 "123.45" -> 123
            if (str.contains(".")) {
                return (long) Double.parseDouble(str);
            }
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 浮点数转换 ====================

    /**
     * 将对象转换为浮点数
     *
     * @param value 对象值
     * @return 浮点数，如果转换失败返回 null
     */
    public static Float toFloat(Object value) {
        return toFloat(value, null);
    }

    /**
     * 将对象转换为浮点数（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 浮点数，如果转换失败返回默认值
     */
    public static Float toFloat(Object value, Float defaultValue) {
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.floatValue();
            }
            case Boolean b -> {
                return b ? 1.0f : 0.0f;
            }
            default -> {
            }
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 将对象转换为双精度浮点数
     *
     * @param value 对象值
     * @return 双精度浮点数，如果转换失败返回 null
     */
    public static Double toDouble(Object value) {
        return toDouble(value, null);
    }

    /**
     * 将对象转换为双精度浮点数（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 双精度浮点数，如果转换失败返回默认值
     */
    public static Double toDouble(Object value, Double defaultValue) {
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.doubleValue();
            }
            case Boolean b -> {
                return b ? 1.0 : 0.0;
            }
            default -> {
            }
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 布尔值转换 ====================

    /**
     * 将对象转换为布尔值
     *
     * @param value 对象值
     * @return 布尔值，如果转换失败返回 null
     */
    public static Boolean toBool(Object value) {
        return toBool(value, null);
    }

    /**
     * 将对象转换为布尔值（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 布尔值，如果转换失败返回默认值
     */
    public static Boolean toBool(Object value, Boolean defaultValue) {
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Boolean b -> {
                return b;
            }
            case Number number -> {
                return number.intValue() != 0;
            }
            default -> {
            }
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        // 转换为小写进行比较
        str = str.toLowerCase();
        // 支持多种布尔值表示
        if ("true".equals(str) || "1".equals(str) || "yes".equals(str) || "on".equals(str) || "y".equals(str)) {
            return true;
        }
        if ("false".equals(str) || "0".equals(str) || "no".equals(str) || "off".equals(str) || "n".equals(str)) {
            return false;
        }
        return defaultValue;
    }

    // ==================== 字节转换 ====================

    /**
     * 将对象转换为字节
     *
     * @param value 对象值
     * @return 字节，如果转换失败返回 null
     */
    public static Byte toByte(Object value) {
        return toByte(value, null);
    }

    /**
     * 将对象转换为字节（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 字节，如果转换失败返回默认值
     */
    public static Byte toByte(Object value, Byte defaultValue) {
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.byteValue();
            }
            case Boolean b -> {
                return b ? (byte) 1 : (byte) 0;
            }
            default -> {
            }
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        try {
            // 处理小数点后的数字
            if (str.contains(".")) {
                return (byte) Double.parseDouble(str);
            }
            return Byte.parseByte(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // ==================== 短整数转换 ====================

    /**
     * 将对象转换为短整数
     *
     * @param value 对象值
     * @return 短整数，如果转换失败返回 null
     */
    public static Short toShort(Object value) {
        return toShort(value, null);
    }

    /**
     * 将对象转换为短整数（带默认值）
     *
     * @param value        对象值
     * @param defaultValue 默认值
     * @return 短整数，如果转换失败返回默认值
     */
    public static Short toShort(Object value, Short defaultValue) {
        switch (value) {
            case null -> {
                return defaultValue;
            }
            case Number number -> {
                return number.shortValue();
            }
            case Boolean b -> {
                return b ? (short) 1 : (short) 0;
            }
            default -> {
            }
        }
        String str = value.toString().trim();
        if (str.isEmpty()) {
            return defaultValue;
        }
        try {
            // 处理小数点后的数字
            if (str.contains(".")) {
                return (short) Double.parseDouble(str);
            }
            return Short.parseShort(str);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
