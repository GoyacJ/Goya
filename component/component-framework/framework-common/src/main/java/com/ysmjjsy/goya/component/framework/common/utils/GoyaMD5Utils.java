package com.ysmjjsy.goya.component.framework.common.utils;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
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
public class GoyaMD5Utils {

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
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).cause(e).build();
        }
    }
}
