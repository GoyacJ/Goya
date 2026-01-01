package com.ysmjjsy.goya.component.common.crypto;


import com.ysmjjsy.goya.component.common.utils.CryptUtils;

/**
 * <p>AES 加密算法处理器</p>
 *
 * @author goya
 * @since 2025/10/9 16:33
 */
public class AesCryptoProcessor implements ISymmetricCryptoProcessor {

    @Override
    public String createKey() {
       return CryptUtils.createAesKey();
    }

    @Override
    public String decrypt(String data, String key) {
        return CryptUtils.decryptAes(data, key);
    }

    @Override
    public String encrypt(String data, String key) {
        return CryptUtils.encryptAes(data, key);
    }
}