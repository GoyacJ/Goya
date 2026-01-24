package com.ysmjjsy.goya.component.framework.crypto.processor;

import com.ysmjjsy.goya.component.framework.crypto.utils.GoyaCryptoUtils;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/9 16:36
 */
public class Sm4CryptoProcessor implements SymmetricCryptoProcessor {

    @Override
    public String createKey() {
        return GoyaCryptoUtils.createSm4key();
    }

    @Override
    public String decrypt(String data, String key) {
        return GoyaCryptoUtils.decryptSm4(data, key);
    }

    @Override
    public String encrypt(String data, String key) {
        return GoyaCryptoUtils.encryptSm4(data, key);
    }
}
