package com.ysmjjsy.goya.component.common.utils;

import com.ysmjjsy.goya.component.common.exception.CryptoException;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/2 19:45
 */
@UtilityClass
public class MD5Utils {

    public static String md5(String str) {
        try {
            // 获取 MD5 摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(str.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new CryptoException("MD5 加密失败", e);
        }
    }
}
