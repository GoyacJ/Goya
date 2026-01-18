package com.ysmjjsy.goya.component.core.enums;

import lombok.Getter;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>常用正则表达式枚举池</p>
 *
 * @author goya
 * @since 2025/12/21 22:42
 */
@Getter
public enum RegexPoolEnum {

    /**
     * 正则：匹配包名分隔符 .
     */
    PACKAGE_SEPARATOR_REGEX("\\."),

    /**
     * 匹配大括号以及其中内容。
     * 示例： "ab{gnfnm}ah{hell}o"
     * 匹配结果：{gnfnm}、{hell}
     */
    BRACES_AND_CONTENT("\\{([^}]*)\\}"),

    /**
     * 匹配所有字符（用于 split 拆分单字符）
     * 示例：String cat = "abc";
     * 调用：cat.split("(?!^)") → ["a", "b", "c"]
     */
    ALL_CHARACTERS("(?!^)"),

    /**
     * 单引号包围的字符串等式
     * 示例：pattern='/open/**'
     */
    SINGLE_QUOTE_STRING_EQUATION("(\\w+)\\s*=\\s*'(.*?)'"),

    /**
     * Bucket DNS 兼容校验（仅支持 a-z0-9.-）
     * 示例：valid-bucket.01
     */
    DNS_COMPATIBLE("^[a-z0-9][a-z0-9\\.\\-]+[a-z0-9]$"),

    /**
     * IPv4 正则表达式
     */
    IPV4_REGEX(
            "^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\." +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\." +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\." +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$"
    ),

    /**
     * 邮箱
     */
    EMAIL("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),

    /**
     * 手机号码（E.164）
     */
    PHONE_NUMBER("^\\+[1-9]\\d{1,14}$"),

    /**
     * 用户名
     */
    USERNAME("^[a-zA-Z][a-zA-Z0-9_-]{2,13}$"),

    /**
     * 密码
     */
    PASSWORD("^[a-zA-Z][a-zA-Z0-9_-]{2,13}$"),

    /**
     * 字母、数字、下划线和连字符
     */
    ALPHANUMERIC_UNDERSCORE_HYPHEN_REGEX("^[a-zA-Z0-9_-]+$");

    private final String regex;
    private final Pattern pattern;

    RegexPoolEnum(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
    }

    /**
     * 完全匹配（matches）
     */
    public boolean matches(String content) {
        if (content == null) {
            return false;
        }
        return pattern.matcher(content).matches();
    }

    /**
     * 包含匹配（find）
     */
    public boolean find(String content) {
        if (content == null) {
            return false;
        }
        return pattern.matcher(content).find();
    }

    /**
     * 静态工具方法：完全匹配
     */
    public static boolean matches(String regex, String content) {
        Objects.requireNonNull(regex, "regex cannot be null");
        if (content == null) {
            return false;
        }
        return Pattern.matches(regex, content);
    }

    /**
     * 静态工具方法：包含匹配
     */
    public static boolean find(String regex, String content) {
        Objects.requireNonNull(regex, "regex cannot be null");
        if (content == null) {
            return false;
        }
        Matcher matcher = Pattern.compile(regex).matcher(content);
        return matcher.find();
    }
}