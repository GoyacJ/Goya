package com.ysmjjsy.goya.component.core.crypto;

import com.ysmjjsy.goya.component.core.utils.GoyaCryptoUtils;

/**
 * <p>AES 加密算法处理器</p>
 *
 * @author goya
 * @since 2025/10/9 16:33
 */
public class AesCryptoProcessor implements SymmetricCryptoProcessor {

    @Override
    public String createKey() {
       return GoyaCryptoUtils.createAesKey();
    }

    @Override
    public String decrypt(String data, String key) {
        return GoyaCryptoUtils.decryptAes(data, key);
    }

    @Override
    public String encrypt(String data, String key) {
        return GoyaCryptoUtils.encryptAes(data, key);
    }
}