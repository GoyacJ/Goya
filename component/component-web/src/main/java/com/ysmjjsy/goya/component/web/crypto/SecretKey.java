package com.ysmjjsy.goya.component.web.crypto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <p>秘钥缓存存储实体</p>
 *
 * @author goya
 * @since 2025/9/24 16:05
 */
public record SecretKey(

        /*
         * 数据存储身份标识
         */
        String identity,

        /*
         * 本系统授权码模式中后台返回的 State
         */
        String state,

        String symmetricKey,
        /*
          服务器端非对称加密算法公钥
          1. RSA 为 Base64 格式
          2. SM2 为 Hex 格式
         */
        @Schema(description = "服务器端非对称加密算法公钥")
        String publicKey,

        @Schema(description = "服务器端非对称加密算法私钥")
        String privateKey,

        @Schema(description = "创建时间戳")
        Timestamp createdAt
) implements Serializable {

    public SecretKey {
        createdAt = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 生成秘钥缓存存储实体
     *
     * @param privateKey 服务器端非对称加密算法私钥
     * @param publicKey  服务器端非对称加密算法公钥
     * @return 秘钥缓存
     */
    public static SecretKey generateKey(String privateKey, String publicKey) {
        return new SecretKey(

                null,
                null,
                null,
                publicKey,
                privateKey,
                new Timestamp(System.currentTimeMillis())
        );
    }

    /**
     * 生成秘钥缓存存储实体
     *
     * @return 秘钥缓存
     */
    public SecretKey generateKey(
            String identity,
            String state,
            String symmetricKey) {
        return new SecretKey(
                identity,
                state,
                symmetricKey,
                publicKey,
                privateKey,
                new Timestamp(System.currentTimeMillis())
        );
    }
}
