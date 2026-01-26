package com.ysmjjsy.goya.component.framework.servlet.configuration;

import com.ysmjjsy.goya.component.framework.crypto.processor.AsymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.framework.crypto.processor.SymmetricCryptoProcessor;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import com.ysmjjsy.goya.component.framework.servlet.crypto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 11:08
 */
@Slf4j
@RequiredArgsConstructor
@Configuration
public class DecryptConfiguration {
    
    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] DecryptConfiguration configure.");
    }

    @Bean
    public CryptoCacheManager cryptoCacheManager(GoyaWebProperties properties, AsymmetricCryptoProcessor asymmetricCryptoProcessor, SymmetricCryptoProcessor symmetricCryptoProcessor) {
        CryptoCacheManager cryptoCacheManager = new CryptoCacheManager(
                properties.crypto(),
                asymmetricCryptoProcessor,
                symmetricCryptoProcessor
        );
        log.trace("[Goya] |- component [framework] DecryptConfiguration |- bean [cryptoCacheManager] register.");
        return cryptoCacheManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public DecryptRequestBodyAdvice decryptRequestBodyAdvice(CryptoCacheManager cryptoCacheManager) {
        DecryptRequestBodyAdvice decryptRequestBodyAdvice = new DecryptRequestBodyAdvice();
        decryptRequestBodyAdvice.setInterfaceCryptoProcessor(cryptoCacheManager);
        log.trace("[Goya] |- component [framework] DecryptConfiguration |- bean [decryptRequestBodyAdvice] register.");
        return decryptRequestBodyAdvice;
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptResponseBodyAdvice encryptResponseBodyAdvice(CryptoCacheManager cryptoCacheManager) {
        EncryptResponseBodyAdvice encryptResponseBodyAdvice = new EncryptResponseBodyAdvice(cryptoCacheManager);
        log.trace("[Goya] |- component [framework] DecryptConfiguration |- bean [encryptResponseBodyAdvice] register.");
        return encryptResponseBodyAdvice;
    }

    @Bean
    @ConditionalOnMissingBean
    public DecryptRequestParamMapResolver decryptRequestParamStringResolver(CryptoCacheManager cryptoCacheManager) {
        DecryptRequestParamMapResolver decryptRequestParamMapResolver = new DecryptRequestParamMapResolver();
        decryptRequestParamMapResolver.setCryptoCacheManager(cryptoCacheManager);
        log.trace("[Goya] |- component [framework] DecryptConfiguration |- bean [decryptRequestParamStringResolver] register.");
        return decryptRequestParamMapResolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public DecryptRequestParamResolver decryptRequestParamResolver(CryptoCacheManager cryptoCacheManager) {
        DecryptRequestParamResolver decryptRequestParamResolver = new DecryptRequestParamResolver();
        decryptRequestParamResolver.setCryptoCacheManager(cryptoCacheManager);
        log.trace("[Goya] |- component [framework] DecryptConfiguration |- bean [decryptRequestParamResolver] register.");
        return decryptRequestParamResolver;
    }
}
