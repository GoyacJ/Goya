package com.ysmjjsy.goya.component.common.crypto;

import com.ysmjjsy.goya.component.common.utils.CryptUtils;
import com.ysmjjsy.goya.component.common.utils.KeyDTO;

/**
 * <p>国密 SM2 算法处理</p>
 *
 * @author goya
 * @since 2025/10/9 16:35
 */
public class Sm2CryptoProcessor implements IAsymmetricCryptoProcessor {

    @Override
    public SecretKey createSecretKey() {
        KeyDTO key = CryptUtils.createSm2Key();
        return SecretKey.generateKey(key.privateKey(), key.publicKey());
    }

    @Override
    public String decrypt(String content, String privateKey) {
        return CryptUtils.decryptSm2(content, privateKey);
    }

    @Override
    public String encrypt(String content, String publicKey) {
        return CryptUtils.encryptSm2(content, publicKey);
    }

}
