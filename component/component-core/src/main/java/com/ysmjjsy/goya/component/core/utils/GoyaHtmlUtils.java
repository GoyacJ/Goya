package com.ysmjjsy.goya.component.core.utils;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * <p>HTML 工具类</p>
 * <p>提供 HTML 标签过滤、转义、清理等功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 20:29
 */
@UtilityClass
public class GoyaHtmlUtils {

    /**
     * HTML 标签正则表达式
     */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);

    /**
     * HTML 注释正则表达式
     */
    private static final Pattern HTML_COMMENT_PATTERN = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

    /**
     * Script 标签及其内容正则表达式
     */
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Style 标签及其内容正则表达式
     */
    private static final Pattern STYLE_PATTERN = Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * 处理空串并过滤HTML标签
     * <p>移除所有 HTML 标签、注释、脚本和样式，只保留纯文本内容</p>
     *
     * @param html HTML 字符串
     * @return 过滤后的纯文本，如果输入为 null 或空字符串则返回空字符串
     */
    public static String cleanHtmlTag(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        String result = html;

        // 移除 script 标签及其内容
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");

        // 移除 style 标签及其内容
        result = STYLE_PATTERN.matcher(result).replaceAll("");

        // 移除 HTML 注释
        result = HTML_COMMENT_PATTERN.matcher(result).replaceAll("");

        // 移除所有 HTML 标签
        result = HTML_TAG_PATTERN.matcher(result).replaceAll("");

        // 解码常见的 HTML 实体
        result = decodeHtmlEntities(result);

        // 清理多余的空白字符
        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }

    /**
     * 解码常见的 HTML 实体
     *
     * @param text 包含 HTML 实体的文本
     * @return 解码后的文本
     */
    private static String decodeHtmlEntities(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        return text
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&copy;", "©")
                .replace("&reg;", "®")
                .replace("&trade;", "™")
                .replace("&nbsp", " ")
                .replace("&lt", "<")
                .replace("&gt", ">")
                .replace("&amp", "&")
                .replace("&quot", "\"");
    }

    /**
     * 转义 HTML 特殊字符
     * <p>将 HTML 特殊字符转换为 HTML 实体</p>
     *
     * @param text 原始文本
     * @return 转义后的 HTML 字符串
     */
    public static String escapeHtml(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 反转义 HTML 实体
     * <p>将 HTML 实体转换回特殊字符</p>
     *
     * @param html HTML 字符串
     * @return 反转义后的文本
     */
    public static String unescapeHtml(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        return decodeHtmlEntities(html);
    }

    /**
     * 移除 HTML 标签但保留文本内容
     * <p>与 cleanHtmlTag 类似，但不会清理空白字符和 HTML 实体</p>
     *
     * @param html HTML 字符串
     * @return 移除标签后的文本
     */
    public static String removeHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        String result = html;

        // 移除 script 标签及其内容
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");

        // 移除 style 标签及其内容
        result = STYLE_PATTERN.matcher(result).replaceAll("");

        // 移除 HTML 注释
        result = HTML_COMMENT_PATTERN.matcher(result).replaceAll("");

        // 移除所有 HTML 标签
        result = HTML_TAG_PATTERN.matcher(result).replaceAll("");

        return result;
    }

    /**
     * 检查字符串是否包含 HTML 标签
     *
     * @param text 文本
     * @return 是否包含 HTML 标签
     */
    public static boolean containsHtmlTags(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return HTML_TAG_PATTERN.matcher(text).find();
    }
}
