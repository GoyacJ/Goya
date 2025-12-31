package com.ysmjjsy.goya.component.common.utils;

import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;

/**
 * <p>字节工具类</p>
 * <p>提供字符串与字节数组之间的转换功能</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 22:31
 */
@UtilityClass
public class ByteUtils {

    /**
     * 将字符串转换为 UTF-8 字节数组
     *
     * @param data 字符串
     * @return UTF-8 字节数组，如果输入为 null 则返回 null
     */
    public static byte[] toUtf8Bytes(String data) {
        if (data == null) {
            return null;
        }
        return data.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 将字符串转换为指定编码的字节数组
     *
     * @param data    字符串
     * @param charset 字符编码名称
     * @return 字节数组，如果输入为 null 则返回 null
     */
    public static byte[] toBytes(String data, String charset) {
        if (data == null) {
            return null;
        }
        try {
            return data.getBytes(charset);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new CommonException("Unsupported charset: " + charset, e);
        }
    }

    /**
     * 将字节数组转换为 UTF-8 字符串
     *
     * @param bytes 字节数组
     * @return UTF-8 字符串，如果输入为 null 则返回 null
     */
    public static String toUtf8String(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 将字节数组转换为指定编码的字符串
     *
     * @param bytes   字节数组
     * @param charset 字符编码名称
     * @return 字符串，如果输入为 null 则返回 null
     */
    public static String toString(byte[] bytes, String charset) {
        if (bytes == null) {
            return null;
        }
        try {
            return new String(bytes, charset);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new CommonException("Unsupported charset: " + charset, e);
        }
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串（小写），如果输入为 null 则返回 null
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串
     * @return 字节数组，如果输入为 null 或空字符串则返回 null
     */
    public static byte[] fromHexString(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return null;
        }
        // 移除可能的前缀（如 0x）
        if (hexString.startsWith("0x") || hexString.startsWith("0X")) {
            hexString = hexString.substring(2);
        }
        // 移除空格和连字符
        hexString = hexString.replaceAll("[\\s-]", "");
        
        int len = hexString.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string length must be even");
        }
        
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }
}
