package com.ysmjjsy.goya.component.common.utils;

import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/29 19:10
 */
@UtilityClass
public class CommonUtils {

    public static String joinComma(String[] value) {
        return StringUtils.join(value, ISymbolConstants.COMMA);
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

}
