package com.ysmjjsy.goya.component.framework.crypto.processor;

import com.ysmjjsy.goya.component.framework.crypto.utils.GoyaCryptoUtils;

/**
 * <p>国密 SM2 算法处理</p>
 *
 * @author goya
 * @since 2025/10/9 16:35
 */
public class Sm2CryptoProcessor implements AsymmetricCryptoProcessor {

    @Override
    public AsymmetricSecretKey createSecretKey() {
        GoyaCryptoUtils.KeyDTO key = GoyaCryptoUtils.createSm2Key();
        return AsymmetricSecretKey.generate(key.privateKey(), key.publicKey());
    }

    @Override
    public String decrypt(String content, String privateKey) {
        return GoyaCryptoUtils.decryptSm2(content, privateKey);
    }

    @Override
    public String encrypt(String content, String publicKey) {
        return GoyaCryptoUtils.encryptSm2(content, publicKey);
    }

}
