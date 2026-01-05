package com.ysmjjsy.goya.security.core.enums;

import org.apache.commons.lang3.Strings;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 13:16
 */
public enum UrlCategoryEnum {

    /**
     * 含有通配符，含有 "*" 或 "?"
     */
    WILDCARD,
    /**
     * 含有占位符，含有 "{" 和 " } "
     */
    PLACEHOLDER,
    /**
     * 不含有任何特殊字符的完整路径
     */
    FULL_PATH;

    public static UrlCategoryEnum getCategory(String url) {

        if (Strings.CS.containsAny(url, "*", "?")) {
            return UrlCategoryEnum.WILDCARD;
        }

        if (Strings.CS.contains(url, "{")) {
            return UrlCategoryEnum.PLACEHOLDER;
        }

        return UrlCategoryEnum.FULL_PATH;
    }
}
