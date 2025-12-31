package com.ysmjjsy.goya.component.web.crypto;

import com.ysmjjsy.goya.component.common.utils.CryptUtils;
import com.ysmjjsy.goya.component.common.utils.KeyDTO;

/**
 * <p>RSA 加密算法处理器</p>
 *
 * @author goya
 * @since 2025/10/9 16:34
 */
public class RsaCryptoProcessor implements IAsymmetricCryptoProcessor {

    @Override
    public SecretKey createSecretKey() {
        KeyDTO key = CryptUtils.createRsaKey();
        return SecretKey.generateKey(key.privateKey(),key.publicKey());
    }

    @Override
    public String decrypt(String content, String privateKey) {
        return CryptUtils.decryptRsa(content, privateKey);
    }

    @Override
    public String encrypt(String content, String publicKey) {
        return CryptUtils.encryptRsa(content, publicKey);
    }
}
