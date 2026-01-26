package com.ysmjjsy.goya.component.framework.servlet.crypto;

import com.ysmjjsy.goya.component.framework.cache.support.CacheSupport;
import com.ysmjjsy.goya.component.framework.common.exception.GoyaException;
import com.ysmjjsy.goya.component.framework.common.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.framework.crypto.processor.AsymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.framework.crypto.processor.AsymmetricSecretKey;
import com.ysmjjsy.goya.component.framework.crypto.processor.SymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

import static com.ysmjjsy.goya.component.framework.servlet.constant.WebConst.CACHE_WEB_PREFIX;

/**
 * <p>crypto缓存管理</p>
 *
 * @author goya
 * @since 2026/1/26 22:40
 */
@Slf4j
public class CryptoCacheManager extends CacheSupport<String, CryptoKey> {

    public static final String CACHE_CRYPTO_PREFIX = CACHE_WEB_PREFIX + "crypto:";
    private final AsymmetricCryptoProcessor asymmetricCryptoProcessor;
    private final SymmetricCryptoProcessor symmetricCryptoProcessor;

    public CryptoCacheManager(GoyaWebProperties.Crypto crypto, AsymmetricCryptoProcessor asymmetricCryptoProcessor, SymmetricCryptoProcessor symmetricCryptoProcessor) {
        super(CACHE_CRYPTO_PREFIX, crypto.expire());
        this.asymmetricCryptoProcessor = asymmetricCryptoProcessor;
        this.symmetricCryptoProcessor = symmetricCryptoProcessor;
    }

    public String encrypt(String identity, String content) {
        try {
            CryptoKey secretKey = getCryptoKey(identity);
            String result = symmetricCryptoProcessor.encrypt(content, secretKey.symmetricKey());
            log.debug("[HZ-ZHG] |- Encrypt content from [{}] to [{}].", content, result);
            return result;
        } catch (GoyaException _) {
            log.warn("[HZ-ZHG] |- Session has expired, need recreate, Skip encrypt content [{}].", content);
            return content;
        } catch (Exception _) {
            log.warn("[HZ-ZHG] |- Symmetric can not Encrypt content [{}], Skip!", content);
            return content;
        }
    }

    public String decrypt(String identity, String content) {
        try {
            CryptoKey secretKey = getCryptoKey(identity);

            String result = symmetricCryptoProcessor.decrypt(content, secretKey.symmetricKey());
            log.debug("[HZ-ZHG] |- Decrypt content from [{}] to [{}].", content, result);
            return result;
        } catch (GoyaException _) {
            log.warn("[HZ-ZHG] |- Session has expired, need recreate, Skip decrypt content [{}].", content);
            return content;
        } catch (Exception _) {
            log.warn("[HZ-ZHG] |- Symmetric can not Decrypt content [{}], Skip!", content);
            return content;
        }
    }

    /**
     * 根据identity创建SecretKey {@link CryptoKey}
     *
     * @param identity                   SessionId，可以为空。
     * @param accessTokenValiditySeconds Session过期时间，单位秒
     * @return {@link CryptoKey}
     */
    public CryptoKey createCryptoKey(String identity, Duration accessTokenValiditySeconds) {
        // 前端如果设置sessionId，则由后端生成
        if (StringUtils.isBlank(identity)) {
            identity = GoyaIdUtils.fastUUID();
        } else {
            try {
                return getCryptoKey(identity);
            } catch (GoyaException _) {
                log.debug("[HZ-ZHG] |- CryptoKey has expired, recreate it");
            }
        }

        // 根据Token的有效时间设置
        Duration expire = getExpire(accessTokenValiditySeconds);
        CryptoKey cryptoKey = hasKey(identity);
        this.put(identity, cryptoKey, expire);
        return cryptoKey;
    }

    public CryptoKey hasKey(String key) {
        AsymmetricSecretKey cryptoKey = asymmetricCryptoProcessor.createSecretKey();
        String symmetricKey = symmetricCryptoProcessor.createKey();
        CryptoKey generateKey = CryptoKey.generateKey(key, GoyaIdUtils.fastUUID(), symmetricKey, cryptoKey.getPrivateKey(), cryptoKey.getPublicKey());

        log.debug("[HZ-ZHG] |- Generate secret key, value is : [{}]", generateKey);
        return generateKey;
    }

    private boolean isSessionValid(String identity) {
        return this.exists(identity);
    }

    private CryptoKey getCryptoKey(String identity) throws GoyaException {
        if (isSessionValid(identity)) {
            CryptoKey cryptoKey = this.get(identity);
            if (ObjectUtils.isNotEmpty(cryptoKey)) {
                log.trace("[HZ-ZHG] |- Decrypt Or Encrypt content use param identity [{}], cached identity is [{}].", identity, cryptoKey.identity());
                return cryptoKey;
            }
        }

        throw new GoyaException("CryptoKey key is expired!");
    }

    private Duration getExpire(Duration accessTokenValiditySeconds) {
        if (ObjectUtils.isEmpty(accessTokenValiditySeconds) || accessTokenValiditySeconds.isZero()) {
            return Duration.ofHours(2L);
        } else {
            return accessTokenValiditySeconds;
        }
    }

    /**
     * 用后端非对称加密算法私钥，解密前端传递过来的、用后端非对称加密算法公钥加密的前端非对称加密算法公钥
     *
     * @param privateKey 后端非对称加密算法私钥
     * @param content    传回的已加密前端非对称加密算法公钥
     * @return 前端非对称加密算法公钥
     */
    private String decryptFrontendPublicKey(String content, String privateKey) {
        String frontendPublicKey = asymmetricCryptoProcessor.decrypt(content, privateKey);
        log.debug("[HZ-ZHG] |- Decrypt frontend public key, value is : [{}]", frontendPublicKey);
        return frontendPublicKey;
    }

    /**
     * 用前端非对称加密算法公钥加密后端生成的对称加密算法 Key
     *
     * @param symmetricKey 对称算法秘钥
     * @param publicKey    前端非对称加密算法公钥
     * @return 用前端前端非对称加密算法公钥加密后的对称算法秘钥
     */
    private String encryptBackendKey(String symmetricKey, String publicKey) {
        String encryptedAesKey = asymmetricCryptoProcessor.encrypt(symmetricKey, publicKey);
        log.debug("[HZ-ZHG] |- Encrypt symmetric key use frontend public key, value is : [{}]", encryptedAesKey);
        return encryptedAesKey;
    }

    /**
     * 前端获取后端生成 AES Key
     *
     * @param identity     Session ID
     * @param confidential 前端和后端加解密结果都
     * @return 前端 PublicKey 加密后的 AES KEY
     * @throws GoyaException sessionId不可用，无法从缓存中找到对应的值
     */
    public String exchange(String identity, String confidential) {
        try {
            CryptoKey cryptoKey = getCryptoKey(identity);
            String frontendPublicKey = decryptFrontendPublicKey(confidential, cryptoKey.privateKey());
            return encryptBackendKey(cryptoKey.symmetricKey(), frontendPublicKey);
        } catch (GoyaException e) {
            throw new GoyaException(e);
        }

    }
}
