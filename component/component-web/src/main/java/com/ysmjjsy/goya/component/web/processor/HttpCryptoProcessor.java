package com.ysmjjsy.goya.component.web.processor;

import com.ysmjjsy.goya.component.cache.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.template.AbstractCheckTemplate;
import com.ysmjjsy.goya.component.cache.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.common.utils.IdentityUtils;
import com.ysmjjsy.goya.component.web.configuration.properties.WebProperties;
import com.ysmjjsy.goya.component.web.constants.IWebConstants;
import com.ysmjjsy.goya.component.web.crypto.IAsymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.web.crypto.ISymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.web.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;

/**
 * <p>接口加密解密处理器</p>
 *
 * @author goya
 * @since 2025/10/9 16:29
 */
@Slf4j
@RequiredArgsConstructor
public class HttpCryptoProcessor extends AbstractCheckTemplate<String, SecretKey> {

    private final IAsymmetricCryptoProcessor asymmetricCryptoProcessor;
    private final ISymmetricCryptoProcessor symmetricCryptoProcessor;
    private final WebProperties webProperties;

    public String encrypt(String identity, String content) {
        try {
            SecretKey secretKey = getSecretKey(identity);
            String result = symmetricCryptoProcessor.encrypt(content, secretKey.symmetricKey());
            log.debug("[GOYA] |- Encrypt content from [{}] to [{}].", content, result);
            return result;
        } catch (CommonException e) {
            log.warn("[GOYA] |- Session has expired, need recreate, Skip encrypt content [{}].", content);
            return content;
        } catch (Exception e) {
            log.warn("[GOYA] |- Symmetric can not Encrypt content [{}], Skip!", content);
            return content;
        }
    }

    public String decrypt(String identity, String content) {
        try {
            SecretKey secretKey = getSecretKey(identity);

            String result = symmetricCryptoProcessor.decrypt(content, secretKey.symmetricKey());
            log.debug("[GOYA] |- Decrypt content from [{}] to [{}].", content, result);
            return result;
        } catch (CommonException e) {
            log.warn("[GOYA] |- Session has expired, need recreate, Skip decrypt content [{}].", content);
            return content;
        } catch (Exception e) {
            log.warn("[GOYA] |- Symmetric can not Decrypt content [{}], Skip!", content);
            return content;
        }
    }

    /**
     * 根据SessionId创建SecretKey {@link SecretKey}。如果前端有可以唯一确定的SessionId，并且使用该值，则用该值创建SecretKey。否则就由后端动态生成一个SessionId。
     *
     * @param identity                   SessionId，可以为空。
     * @param accessTokenValiditySeconds Session过期时间，单位秒
     * @return {@link SecretKey}
     */
    public SecretKey createSecretKey(String identity, Duration accessTokenValiditySeconds) {
        // 前端如果设置sessionId，则由后端生成
        if (StringUtils.isBlank(identity)) {
            identity = IdentityUtils.fastUUID();
        } else {
            try {
                return getSecretKey(identity);
            } catch (CommonException e) {
                log.debug("[GOYA] |- SecretKey has expired, recreate it");
            }
        }

        // 根据Token的有效时间设置
        Duration expire = getExpire(accessTokenValiditySeconds);
        return this.put(identity, expire);
    }

    private boolean isSessionValid(String identity) {
        return this.exists(identity);
    }

    private SecretKey getSecretKey(String identity) throws CommonException {
        if (isSessionValid(identity)) {
            SecretKey secretKey = this.get(identity);
            if (ObjectUtils.isNotEmpty(secretKey)) {
                log.trace("[GOYA] |- Decrypt Or Encrypt content use param identity [{}], cached identity is [{}].", identity, secretKey.identity());
                return secretKey;
            }
        }

        throw new CommonException("SecretKey key is expired!");
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
        log.debug("[GOYA] |- Decrypt frontend public key, value is : [{}]", frontendPublicKey);
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
        log.debug("[GOYA] |- Encrypt symmetric key use frontend public key, value is : [{}]", encryptedAesKey);
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
        try {
            SecretKey secretKey = getSecretKey(identity);
            String frontendPublicKey = decryptFrontendPublicKey(confidential, secretKey.privateKey());
            return encryptBackendKey(secretKey.symmetricKey(), frontendPublicKey);
        } catch (CommonException e) {
            throw new CommonException();
        }

    }

    @Override
    protected SecretKey nextValue(String key) {
        SecretKey secretKey = asymmetricCryptoProcessor.createSecretKey();
        String symmetricKey = symmetricCryptoProcessor.createKey();
        SecretKey generateKey = secretKey.generateKey(key, IdentityUtils.fastUUID(), symmetricKey);

        log.debug("[GOYA] |- Generate secret key, value is : [{}]", generateKey);
        return generateKey;
    }

    @Override
    protected String getCacheName() {
        return IWebConstants.CACHE_SECURE_KEY_PREFIX;
    }

    @Override
    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(webProperties.cryptoExpire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }
}
