package com.ysmjjsy.goya.component.framework.servlet.crypto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.Timestamp;

/**
 * <p>秘钥缓存存储实体</p>
 *
 * @author goya
 * @since 2026/1/26 22:41
 */
public record CryptoKey(
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
) {

    public CryptoKey {
        createdAt = new Timestamp(System.currentTimeMillis());
    }

    /**
     * 生成秘钥缓存存储实体
     *
     * @param privateKey 服务器端非对称加密算法私钥
     * @param publicKey  服务器端非对称加密算法公钥
     * @return 秘钥缓存
     */
    public static CryptoKey generateKey(String privateKey, String publicKey) {
        return new CryptoKey(
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
    public CryptoKey generateKey(
            String identity,
            String state,
            String symmetricKey) {
        return new CryptoKey(
                identity,
                state,
                symmetricKey,
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
    public static CryptoKey generateKey(
            String identity,
            String state,
            String symmetricKey,
            String privateKey,
            String publicKey) {
        return new CryptoKey(
                identity,
                state,
                symmetricKey,
                publicKey,
                privateKey,
                new Timestamp(System.currentTimeMillis())
        );
    }
}
