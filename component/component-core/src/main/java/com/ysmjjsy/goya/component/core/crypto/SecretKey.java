package com.ysmjjsy.goya.component.core.crypto;

import java.io.Serializable;

/**
 * <p>秘钥缓存存储实体</p>
 *
 * @author goya
 * @since 2025/9/24 16:05
 */
public record SecretKey(
        /*
          服务器端非对称加密算法公钥
          1. RSA 为 Base64 格式
          2. SM2 为 Hex 格式
         */
        String publicKey,

        /*
          服务器端非对称加密算法私钥
         */
        String privateKey
) implements Serializable {

    /**
     * 生成秘钥缓存存储实体
     *
     * @param privateKey 服务器端非对称加密算法私钥
     * @param publicKey  服务器端非对称加密算法公钥
     * @return 秘钥缓存
     */
    public static SecretKey generateKey(String privateKey, String publicKey) {
        return new SecretKey(
                publicKey,
                privateKey
        );
    }
}
