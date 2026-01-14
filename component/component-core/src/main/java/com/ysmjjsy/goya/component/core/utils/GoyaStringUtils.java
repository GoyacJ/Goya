package com.ysmjjsy.goya.component.core.utils;

import com.google.common.base.Strings;
import com.ysmjjsy.goya.component.core.constants.SymbolConst;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 22:37
 */
@UtilityClass
public class GoyaStringUtils {

    /* ==================== 空值 / 空白判断 ==================== */

    /**
     * 是否为 null 或空字符串
     */
    public static boolean isEmpty(String value) {
        return StringUtils.isEmpty(value);
    }

    /**
     * 是否为 null、空字符串或仅包含空白字符
     */
    public static boolean isBlank(String value) {
        return StringUtils.isBlank(value);
    }

    /**
     * 是否非空（非 null 且长度 > 0）
     */
    public static boolean isNotEmpty(String value) {
        return StringUtils.isNotEmpty(value);
    }

    /**
     * 是否非空白
     */
    public static boolean isNotBlank(String value) {
        return StringUtils.isNotBlank(value);
    }

    /* ==================== 默认值处理 ==================== */

    /**
     * null 转空字符串
     */
    public static String nullToEmpty(String value) {
        return Strings.nullToEmpty(value);
    }

    /**
     * 空字符串转 null
     */
    public static String emptyToNull(String value) {
        return Strings.emptyToNull(value);
    }

    /**
     * 如果字符串为空白，返回默认值
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.defaultIfBlank(value, defaultValue);
    }

    /**
     * 如果字符串为空，返回默认值
     */
    public static String defaultIfEmpty(String value, String defaultValue) {
        return StringUtils.defaultIfEmpty(value, defaultValue);
    }

    /* ==================== 裁剪 / 清洗 ==================== */

    /**
     * 去除首尾空白（null 安全）
     */
    public static String trim(String value) {
        return StringUtils.trim(value);
    }

    /**
     * 去除首尾空白，结果为空则返回 null
     */
    public static String trimToNull(String value) {
        return StringUtils.trimToNull(value);
    }

    /**
     * 去除首尾空白，结果为空则返回空字符串
     */
    public static String trimToEmpty(String value) {
        return StringUtils.trimToEmpty(value);
    }

    /**
     * 拼接逗号
     * @param value
     * @return
     */
    public static String joinComma(String[] value) {
        return StringUtils.join(value, SymbolConst.COMMA);
    }
    /**
     * 驼峰转下划线
     * @param str
     * @return
     */
    public static String humpToLine(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static boolean startsWithAny(final CharSequence string, final CharSequence... searchStrings) {
        return org.apache.commons.lang3.Strings.CS.startsWithAny(string, searchStrings);
    }

    public static boolean endsWithAny(final CharSequence string, final CharSequence... searchStrings) {
        return org.apache.commons.lang3.Strings.CS.equalsAny(string, searchStrings);
    }

    public static boolean startsWithAnyIgnoreCase(final CharSequence string, final CharSequence... searchStrings) {
        return org.apache.commons.lang3.Strings.CI.startsWithAny(string, searchStrings);
    }

    public static boolean endsWithAnyIgnoreCase(final CharSequence string, final CharSequence... searchStrings) {
        return org.apache.commons.lang3.Strings.CI.equalsAny(string, searchStrings);
    }

    public static boolean containsAnyIgnoreCase(final CharSequence string, final CharSequence... searchStrings) {
        return org.apache.commons.lang3.Strings.CI.containsAny(string, searchStrings);
    }

    public static boolean containsAny(final CharSequence string, final CharSequence... searchStrings) {
        return org.apache.commons.lang3.Strings.CS.containsAny(string, searchStrings);
    }

    public static boolean equalsIgnoreCase(String str1, String str2) {
        return org.apache.commons.lang3.Strings.CI.equals(str1, str2);
    }

    public static boolean equals(String str1, String str2) {
        return org.apache.commons.lang3.Strings.CS.equals(str1, str2);
    }
}
