package com.ysmjjsy.goya.component.cache.multilevel.crypto;

import com.ysmjjsy.goya.component.cache.core.constants.CacheConst;
import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.core.crypto.AsymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.core.crypto.SecretKey;
import com.ysmjjsy.goya.component.core.crypto.SymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.core.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.framework.configuration.properties.CryptoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

/**
 * <p>加密解密处理器</p>
 *
 * @author goya
 * @since 2025/10/9 16:29
 */
@Slf4j
@RequiredArgsConstructor
public class CryptoProcessor extends AbstractCheckTemplate<String, SecretKey> {

    private final AsymmetricCryptoProcessor asymmetricCryptoProcessor;
    private final SymmetricCryptoProcessor symmetricCryptoProcessor;
    private final CryptoProperties cryptoProperties;

    public String encrypt(String identity, String content) {
        try {
            SecretKey secretKey = getSecretKey(identity);
            String result = symmetricCryptoProcessor.encrypt(content, secretKey.symmetricKey());
            log.debug("[Goya] |- Encrypt content from [{}] to [{}].", content, result);
            return result;
        } catch (CommonException _) {
            log.warn("[Goya] |- identity has expired, need recreate, Skip encrypt content [{}].", content);
            return content;
        } catch (Exception _) {
            log.warn("[Goya] |- Symmetric can not Encrypt content [{}], Skip!", content);
            return content;
        }
    }

    public String decrypt(String identity, String content) {
        try {
            SecretKey secretKey = getSecretKey(identity);

            String result = symmetricCryptoProcessor.decrypt(content, secretKey.symmetricKey());
            log.debug("[Goya] |- Decrypt content from [{}] to [{}].", content, result);
            return result;
        } catch (CommonException _) {
            log.warn("[Goya] |- identity has expired, need recreate, Skip decrypt content [{}].", content);
            return content;
        } catch (Exception _) {
            log.warn("[Goya] |- Symmetric can not Decrypt content [{}], Skip!", content);
            return content;
        }
    }

    /**
     * 根据identity创建SecretKey {@link SecretKey}。如果前端有可以唯一确定的identity，并且使用该值，则用该值创建SecretKey。否则就由后端动态生成一个identity。
     *
     * @param identity identity，可以为空。
     * @param expire   过期时间，单位秒
     * @return {@link SecretKey}
     */
    public SecretKey createSecretKey(String identity, Duration expire) {
        // 前端如果设置sessionId，则由后端生成
        if (StringUtils.isBlank(identity)) {
            identity = GoyaIdUtils.fastUUID();
        } else {
            try {
                return getSecretKey(identity);
            } catch (CommonException _) {
                log.debug("[Goya] |- SecretKey has expired, recreate it");
            }
        }

        return this.put(identity, expire);
    }

    private SecretKey getSecretKey(String requestId) throws CommonException {
        if (exists(requestId)) {
            SecretKey secretKey = this.get(requestId);
            if (ObjectUtils.isNotEmpty(secretKey)) {
                log.trace("[Goya] |- Decrypt Or Encrypt content use param identity [{}], cached requestId is [{}].", requestId, secretKey.identity());
                return secretKey;
            }
        }

        throw new CommonException("SecretKey key is expired!");
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
        log.debug("[Goya] |- Decrypt frontend public key, value is : [{}]", frontendPublicKey);
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
        log.debug("[Goya] |- Encrypt symmetric key use frontend public key, value is : [{}]", encryptedAesKey);
        return encryptedAesKey;
    }

    /**
     * 前端获取后端生成 AES Key
     *
     * @param identity     Session ID
     * @param confidential 前端和后端加解密结果都
     * @return 前端 PublicKey 加密后的 AES KEY
     * @throws CommonException sessionId不可用，无法从缓存中找到对应的值
     */
    public String exchange(String identity, String confidential) {
        SecretKey secretKey = getSecretKey(identity);
        String frontendPublicKey = decryptFrontendPublicKey(confidential, secretKey.privateKey());
        return encryptBackendKey(secretKey.symmetricKey(), frontendPublicKey);
    }

    @Override
    protected SecretKey nextValue(String key) {
        SecretKey secretKey = asymmetricCryptoProcessor.createSecretKey();
        String symmetricKey = symmetricCryptoProcessor.createKey();
        SecretKey generateKey = secretKey.generateKey(key, GoyaIdUtils.fastUUID(), symmetricKey);

        log.debug("[Goya] |- Generate secret key, value is : [{}]", generateKey);
        return generateKey;
    }

    @Override
    protected String getCacheName() {
        return CacheConst.CACHE_SECURE_KEY_PREFIX;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(cryptoProperties.expire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }
}
