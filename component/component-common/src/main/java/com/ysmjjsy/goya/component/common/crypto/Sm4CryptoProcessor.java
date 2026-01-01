package com.ysmjjsy.goya.component.common.crypto;

import com.ysmjjsy.goya.component.common.utils.CryptUtils;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/9 16:36
 */
public class Sm4CryptoProcessor implements ISymmetricCryptoProcessor {

    @Override
    public String createKey() {
        return CryptUtils.createSm4key();
    }

    @Override
    public String decrypt(String data, String key) {
        return CryptUtils.decryptSm4(data, key);
    }

    @Override
    public String encrypt(String data, String key) {
        return CryptUtils.encryptSm4(data, key);
    }
}
