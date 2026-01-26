package com.ysmjjsy.goya.component.framework.masker.core;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * <p>脱敏规则集合</p>
 * @author goya
 * @since 2026/1/24 22:01
 */
@UtilityClass
public final class MaskingRules {

    private static final Pattern EMAIL = Pattern.compile("^(?<u>[^@]{1,})@(?<d>.+)$");
    private static final Pattern PHONE = Pattern.compile("^\\d{11}$");

    /** 密码：固定替换。 */
    public static String password(String raw) {
        return raw == null ? null : "******";
    }

    /** token/密钥：保留前3后3。 */
    public static String token(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String s = raw.trim();
        if (s.length() <= 8) {
            return "****";
        }
        return s.substring(0, 3) + "****" + s.substring(s.length() - 3);
    }

    /** 邮箱：u***@domain。 */
    public static String email(String raw) {
        if (raw == null) {
            return null;
        }
        var m = EMAIL.matcher(raw.trim());
        if (!m.matches()) {
            return generic(raw);
        }
        String u = m.group("u");
        String d = m.group("d");
        if (u.length() <= 1) {
            return "*" + "@" + d;
        }
        return u.charAt(0) + "***@" + d;
    }

    /** 手机：前三后四。 */
    public static String phone(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (!PHONE.matcher(s).matches()) {
            return generic(raw);
        }
        return s.substring(0, 3) + "****" + s.substring(7);
    }

    /** 通用：保留前2后2。 */
    public static String generic(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String s = raw.trim();
        if (s.length() <= 4) {
            return "****";
        }
        return s.substring(0, 2) + "****" + s.substring(s.length() - 2);
    }
}