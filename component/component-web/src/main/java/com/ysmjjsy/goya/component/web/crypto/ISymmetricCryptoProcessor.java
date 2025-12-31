package com.ysmjjsy.goya.component.web.crypto;

/**
 * <p>对称加密算法</p>
 *
 * @author goya
 * @since 2025/10/9 16:31
 */
public interface ISymmetricCryptoProcessor {

    /**
     * 创建 SM4 Key。可以为 16 进制串或字节数组，要求为 128 比特
     *
     * @return SM4 Key
     */
    String createKey();

    /**
     * 用私钥解密
     *
     * @param key  对称算法 秘钥
     * @param data 待解密数据
     * @return 解密后的数据
     */
    String decrypt(String data, String key);

    /**
     * 用公钥加密
     *
     * @param key  对称算法 秘钥
     * @param data 待加密数据
     * @return 加密后的数据
     */
    String encrypt(String data, String key);
}
