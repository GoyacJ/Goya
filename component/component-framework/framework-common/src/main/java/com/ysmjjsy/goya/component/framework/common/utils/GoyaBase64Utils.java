package com.ysmjjsy.goya.component.framework.common.utils;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import lombok.experimental.UtilityClass;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * <p>Base64 编码/解码工具类</p>
 * <p>提供标准 Base64、URL 安全 Base64、MIME Base64 等编码/解码功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 20:14
 */
@UtilityClass
public class GoyaBase64Utils {

    public static final String BASE_64 = ";base64,";

    // ==================== 标准 Base64 编码/解码 ====================

    /**
     * 将字节数组编码为 Base64 字符串
     *
     * @param data 字节数组
     * @return Base64 字符串
     */
    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 将字符串编码为 Base64 字符串（UTF-8）
     *
     * @param data 字符串
     * @return Base64 字符串
     */
    public static String encode(String data) {
        return encode(data, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串编码为 Base64 字符串（指定编码）
     *
     * @param data    字符串
     * @param charset 字符编码
     * @return Base64 字符串
     */
    public static String encode(String data, Charset charset) {
        if (data == null) {
            return null;
        }
        return encode(data.getBytes(charset));
    }

    /**
     * 将 Base64 字符串解码为字节数组
     *
     * @param base64 Base64 字符串
     * @return 字节数组
     * @throws GoyaException 如果解码失败
     */
    public static byte[] decode(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new byte[0];
        }
        try {
            // 移除可能的前缀（如 data:image/png;base64,）
            String cleanBase64 = removePrefix(base64);
            return Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 将 Base64 字符串解码为字符串（UTF-8）
     *
     * @param base64 Base64 字符串
     * @return 解码后的字符串
     * @throws GoyaException 如果解码失败
     */
    public static String decodeToString(String base64) {
        return decodeToString(base64, StandardCharsets.UTF_8);
    }

    /**
     * 将 Base64 字符串解码为字符串（指定编码）
     *
     * @param base64  Base64 字符串
     * @param charset 字符编码
     * @return 解码后的字符串
     * @throws GoyaException 如果解码失败
     */
    public static String decodeToString(String base64, Charset charset) {
        byte[] bytes = decode(base64);
        return new String(bytes, charset);
    }

    // ==================== URL 安全 Base64 编码/解码 ====================

    /**
     * 将字节数组编码为 URL 安全的 Base64 字符串
     * <p>URL 安全 Base64 使用 '-' 和 '_' 替代 '+' 和 '/'，并且不包含填充字符 '='</p>
     *
     * @param data 字节数组
     * @return URL 安全的 Base64 字符串
     */
    public static String encodeUrlSafe(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * 将字符串编码为 URL 安全的 Base64 字符串（UTF-8）
     *
     * @param data 字符串
     * @return URL 安全的 Base64 字符串
     */
    public static String encodeUrlSafe(String data) {
        return encodeUrlSafe(data, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串编码为 URL 安全的 Base64 字符串（指定编码）
     *
     * @param data    字符串
     * @param charset 字符编码
     * @return URL 安全的 Base64 字符串
     */
    public static String encodeUrlSafe(String data, Charset charset) {
        if (data == null) {
            return null;
        }
        return encodeUrlSafe(data.getBytes(charset));
    }

    /**
     * 将 URL 安全的 Base64 字符串解码为字节数组
     *
     * @param base64 URL 安全的 Base64 字符串
     * @return 字节数组
     * @throws GoyaException 如果解码失败
     */
    public static byte[] decodeUrlSafe(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getUrlDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 将 URL 安全的 Base64 字符串解码为字符串（UTF-8）
     *
     * @param base64 URL 安全的 Base64 字符串
     * @return 解码后的字符串
     * @throws GoyaException 如果解码失败
     */
    public static String decodeUrlSafeToString(String base64) {
        return decodeUrlSafeToString(base64, StandardCharsets.UTF_8);
    }

    /**
     * 将 URL 安全的 Base64 字符串解码为字符串（指定编码）
     *
     * @param base64  URL 安全的 Base64 字符串
     * @param charset 字符编码
     * @return 解码后的字符串
     * @throws GoyaException 如果解码失败
     */
    public static String decodeUrlSafeToString(String base64, Charset charset) {
        byte[] bytes = decodeUrlSafe(base64);
        return new String(bytes, charset);
    }

    // ==================== MIME Base64 编码/解码 ====================

    /**
     * 将字节数组编码为 MIME Base64 字符串
     * <p>MIME Base64 每行最多 76 个字符，使用 CRLF 作为行分隔符</p>
     *
     * @param data 字节数组
     * @return MIME Base64 字符串
     */
    public static String encodeMime(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getMimeEncoder().encodeToString(data);
    }

    /**
     * 将字符串编码为 MIME Base64 字符串（UTF-8）
     *
     * @param data 字符串
     * @return MIME Base64 字符串
     */
    public static String encodeMime(String data) {
        return encodeMime(data, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串编码为 MIME Base64 字符串（指定编码）
     *
     * @param data    字符串
     * @param charset 字符编码
     * @return MIME Base64 字符串
     */
    public static String encodeMime(String data, Charset charset) {
        if (data == null) {
            return null;
        }
        return encodeMime(data.getBytes(charset));
    }

    /**
     * 将 MIME Base64 字符串解码为字节数组
     *
     * @param base64 MIME Base64 字符串
     * @return 字节数组
     * @throws GoyaException 如果解码失败
     */
    public static byte[] decodeMime(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getMimeDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }

    /**
     * 将 MIME Base64 字符串解码为字符串（UTF-8）
     *
     * @param base64 MIME Base64 字符串
     * @return 解码后的字符串
     * @throws GoyaException 如果解码失败
     */
    public static String decodeMimeToString(String base64) {
        return decodeMimeToString(base64, StandardCharsets.UTF_8);
    }

    /**
     * 将 MIME Base64 字符串解码为字符串（指定编码）
     *
     * @param base64  MIME Base64 字符串
     * @param charset 字符编码
     * @return 解码后的字符串
     * @throws GoyaException 如果解码失败
     */
    public static String decodeMimeToString(String base64, Charset charset) {
        byte[] bytes = decodeMime(base64);
        return new String(bytes, charset);
    }

    // ==================== 带前缀的 Base64 处理 ====================

    /**
     * 检查是否为带前缀的 Base64 字符串
     *
     * @param base64 Base64 字符串
     * @return 是否带前缀
     */
    public static boolean hasPrefix(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }
        return base64.contains(BASE_64) || base64.contains(":") && base64.contains(",");
    }

    /**
     * 移除 Base64 字符串的前缀
     * <p>支持格式：data:image/png;base64,xxx 或 data:xxx,xxx</p>
     *
     * @param base64 Base64 字符串（可能带前缀）
     * @return 纯 Base64 数据
     */
    public static String removePrefix(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return base64;
        }
        int index = base64.indexOf(BASE_64);
        if (index != -1) {
            return base64.substring(index + 8);
        }
        index = base64.indexOf(",");
        if (index != -1) {
            return base64.substring(index + 1);
        }
        return base64;
    }

    /**
     * 添加 Base64 前缀
     *
     * @param base64   Base64 字符串
     * @param mimeType MIME 类型（如：image/png, image/jpeg）
     * @return 带前缀的 Base64 字符串
     */
    public static String addPrefix(String base64, String mimeType) {
        if (base64 == null) {
            return null;
        }
        if (mimeType == null || mimeType.isEmpty()) {
            return base64;
        }
        return "data:" + mimeType + BASE_64 + base64;
    }

    /**
     * 从带前缀的 Base64 字符串中提取 MIME 类型
     *
     * @param base64 带前缀的 Base64 字符串
     * @return MIME 类型，如果不存在返回 null
     */
    public static String extractMimeType(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        if (!base64.startsWith("data:")) {
            return null;
        }
        int mimeEnd = base64.indexOf(BASE_64);
        if (mimeEnd == -1) {
            mimeEnd = base64.indexOf(",");
        }
        if (mimeEnd == -1) {
            return null;
        }
        return base64.substring(5, mimeEnd);
    }

    // ==================== 工具方法 ====================

    /**
     * 检查字符串是否为有效的 Base64 格式
     *
     * @param base64 Base64 字符串
     * @return 是否为有效的 Base64 格式
     */
    public static boolean isValid(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }
        try {
            String cleanBase64 = removePrefix(base64);
            Base64.getDecoder().decode(cleanBase64);
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }

    /**
     * 检查字符串是否为有效的 URL 安全 Base64 格式
     *
     * @param base64 Base64 字符串
     * @return 是否为有效的 URL 安全 Base64 格式
     */
    public static boolean isValidUrlSafe(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return false;
        }
        try {
            Base64.getUrlDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException _) {
            return false;
        }
    }
}
