package com.ysmjjsy.goya.component.common.definition.constants;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>常用正则表达式</p>
 *
 * @author goya
 * @since 2025/12/21 22:42
 */
public interface IRegexPoolConstants {

    /**
     * 正则：匹配包名分隔符 .
     */
    String PACKAGE_SEPARATOR_REGEX = "\\.";

    /**
     * 匹配大括号以及其中内容。
     * 示例： "ab{gnfnm}ah{hell}o"
     * 匹配结果：{gnfnm}、{hell}
     */
    String BRACES_AND_CONTENT = "\\{([^}]*)\\}";

    /**
     * 匹配所有字符（用于 split 拆分单字符）
     * 示例：String cat = "abc";
     * 调用：cat.split("(?!^)") → ["a", "b", "c"]
     */
    String ALL_CHARACTERS = "(?!^)";

    /**
     * 单引号包围的字符串等式
     * 示例：pattern='/open/**'
     * 匹配结果：pattern 和 /open/**
     */
    String SINGLE_QUOTE_STRING_EQUATION = "(\\w+)\\s*=\\s*'(.*?)'";

    /**
     * Bucket DNS 兼容校验（仅支持 a-z0-9.-）
     * 示例：valid-bucket.01
     */
    String DNS_COMPATIBLE = "^[a-z0-9][a-z0-9\\.\\-]+[a-z0-9]$";

    /**
     * IPv4 正则表达式
     * 规则：
     * - 4 个 0~255 的数字，用 "." 分隔
     * - 不允许前导 0（除了 0 本身）
     */
    String IPV4_REGEX =
            "^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\." +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\." +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\." +
                    "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)$";

    /**
     * 邮箱
     */
    String EMAIL = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\\\.[A-Za-z]{2,}$";
    Pattern EMAIL_PATTERN = Pattern.compile(EMAIL);

    /**
     * 手机号码
     */
    String PHONE_NUMBER = "^\\\\+[1-9]\\\\d{1,14}$";
    Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER);

    /**
     * 用户名
     */
    String USERNAME = "^[a-zA-Z][a-zA-Z0-9_-]{2,13}$";
    Pattern USERNAME_PATTERN = Pattern.compile(USERNAME);

    /**
     * 用户名
     */
    String PASSWORD = "^[a-zA-Z][a-zA-Z0-9_-]{2,13}$";
    Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD);

    /**
     * 字母、数字、下划线和连字符
     */
    String ALPHANUMERIC_UNDERSCORE_HYPHEN_REGEX = "^[a-zA-Z0-9_-]+$";

    Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    /**
     * 判断内容是否匹配给定正则。
     *
     * @param regex   正则表达式，不能为空
     * @param content 待匹配内容，可为空（返回 false）
     * @return 是否匹配成功
     */
    static boolean matcher(String regex, String content) {
        Objects.requireNonNull(regex, "regex cannot be null");

        if (content == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }

    /**
     * 判断内容是否包含匹配项（类似 find()）
     *
     * @param regex   正则表达式
     * @param content 待匹配内容
     * @return 是否包含匹配项
     */
    static boolean find(String regex, String content) {
        Objects.requireNonNull(regex, "regex cannot be null");

        if (content == null) {
            return false;
        }

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }
}
