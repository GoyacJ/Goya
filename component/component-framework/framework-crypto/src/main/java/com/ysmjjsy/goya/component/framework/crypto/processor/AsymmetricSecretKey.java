package com.ysmjjsy.goya.component.framework.crypto.processor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>秘钥缓存存储实体</p>
 *
 * @author goya
 * @since 2025/9/24 16:05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsymmetricSecretKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 0L;

    /**
     * 非对称加密算法公钥
     * 1. RSA 为 Base64 格式
     * 2. SM2 为 Hex 格式
     */
    private String publicKey;

    /**
     * 非对称加密算法私钥
     */
    private String privateKey;

    /**
     * 生成非对称加密算法秘钥
     * @param publicKey 非对称加密算法公钥
     * @param privateKey 非对称加密算法私钥
     * @return SecretKey
     */
    public static AsymmetricSecretKey generate(String publicKey, String privateKey) {
        return new AsymmetricSecretKey(publicKey, privateKey);
    }
}
