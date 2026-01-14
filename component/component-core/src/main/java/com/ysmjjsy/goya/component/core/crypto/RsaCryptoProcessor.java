package com.ysmjjsy.goya.component.core.crypto;

import com.ysmjjsy.goya.component.core.utils.GoyaCryptoUtils;

/**
 * <p>RSA 加密算法处理器</p>
 *
 * @author goya
 * @since 2025/10/9 16:34
 */
public class RsaCryptoProcessor implements AsymmetricCryptoProcessor {

    @Override
    public SecretKey createSecretKey() {
        GoyaCryptoUtils.KeyDTO key = GoyaCryptoUtils.createRsaKey();
        return SecretKey.generateKey(key.privateKey(),key.publicKey());
    }

    @Override
    public String decrypt(String content, String privateKey) {
        return GoyaCryptoUtils.decryptRsa(content, privateKey);
    }

    @Override
    public String encrypt(String content, String publicKey) {
        return GoyaCryptoUtils.encryptRsa(content, publicKey);
    }
}
