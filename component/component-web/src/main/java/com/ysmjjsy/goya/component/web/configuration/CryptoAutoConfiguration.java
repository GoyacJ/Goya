package com.ysmjjsy.goya.component.web.configuration;

import com.ysmjjsy.goya.component.web.advice.DecryptRequestBodyAdvice;
import com.ysmjjsy.goya.component.web.advice.EncryptResponseBodyAdvice;
import com.ysmjjsy.goya.component.web.annotation.CryptoStrategy;
import com.ysmjjsy.goya.component.web.cache.AccessLimitedCacheManager;
import com.ysmjjsy.goya.component.web.cache.IdempotentCacheManager;
import com.ysmjjsy.goya.component.web.configuration.properties.WebProperties;
import com.ysmjjsy.goya.component.web.crypto.*;
import com.ysmjjsy.goya.component.web.enums.CryptoStrategyEnum;
import com.ysmjjsy.goya.component.web.processor.HttpCryptoProcessor;
import com.ysmjjsy.goya.component.web.resolver.DecryptRequestParamMapResolver;
import com.ysmjjsy.goya.component.web.resolver.DecryptRequestParamResolver;
import com.ysmjjsy.goya.component.web.secure.AccessLimitedInterceptor;
import com.ysmjjsy.goya.component.web.secure.IdempotentInterceptor;
import com.ysmjjsy.goya.component.web.secure.XssHttpServletFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 09:58
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class CryptoAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[GOYA] |- component [web] CryptoAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentCacheManager idempotentCacheManager(WebProperties webProperties) {
        IdempotentCacheManager idempotentStampManager = new IdempotentCacheManager(webProperties.idempotent());
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [idempotentStampManager] register.");
        return idempotentStampManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public AccessLimitedCacheManager accessLimitedCacheManager(WebProperties webProperties) {
        AccessLimitedCacheManager accessLimitedStampManager = new AccessLimitedCacheManager(webProperties.accessLimited());
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [accessLimitedStampManager] register.");
        return accessLimitedStampManager;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(IdempotentCacheManager.class)
    public IdempotentInterceptor idempotentInterceptor(IdempotentCacheManager idempotentCacheManager) {
        IdempotentInterceptor interceptor = new IdempotentInterceptor(idempotentCacheManager);
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [idempotentInterceptor] register.");
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AccessLimitedCacheManager.class)
    public AccessLimitedInterceptor accessLimitedInterceptor(AccessLimitedCacheManager accessLimitedCacheManager) {
        AccessLimitedInterceptor interceptor = new AccessLimitedInterceptor(accessLimitedCacheManager);
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [accessLimitedInterceptor] register.");
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public XssHttpServletFilter xssHttpServletFilter() {
        XssHttpServletFilter filter = new XssHttpServletFilter();
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [xssHttpServletFilter] register.");
        return filter;
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpCryptoProcessor httpCryptoProcessor(IAsymmetricCryptoProcessor asymmetricCryptoProcessor,
                                                   ISymmetricCryptoProcessor symmetricCryptoProcessor,
                                                   WebProperties webProperties) {
        HttpCryptoProcessor httpCryptoProcessor = new HttpCryptoProcessor(asymmetricCryptoProcessor, symmetricCryptoProcessor, webProperties);
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [httpCryptoProcessor] register.");
        return httpCryptoProcessor;
    }

    @Bean
    @ConditionalOnClass(HttpCryptoProcessor.class)
    @ConditionalOnMissingBean
    public DecryptRequestBodyAdvice decryptRequestBodyAdvice(HttpCryptoProcessor httpCryptoProcessor) {
        DecryptRequestBodyAdvice decryptRequestBodyAdvice = new DecryptRequestBodyAdvice();
        decryptRequestBodyAdvice.setInterfaceCryptoProcessor(httpCryptoProcessor);
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [decryptRequestBodyAdvice] register.");
        return decryptRequestBodyAdvice;
    }

    @Bean
    @ConditionalOnClass(HttpCryptoProcessor.class)
    @ConditionalOnMissingBean
    public EncryptResponseBodyAdvice encryptResponseBodyAdvice(HttpCryptoProcessor httpCryptoProcessor) {
        EncryptResponseBodyAdvice encryptResponseBodyAdvice = new EncryptResponseBodyAdvice(httpCryptoProcessor);
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [encryptResponseBodyAdvice] register.");
        return encryptResponseBodyAdvice;
    }

    @Bean
    @ConditionalOnClass(HttpCryptoProcessor.class)
    @ConditionalOnMissingBean
    public DecryptRequestParamMapResolver decryptRequestParamStringResolver(HttpCryptoProcessor httpCryptoProcessor) {
        DecryptRequestParamMapResolver decryptRequestParamMapResolver = new DecryptRequestParamMapResolver();
        decryptRequestParamMapResolver.setHttpCryptoProcessor(httpCryptoProcessor);
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [decryptRequestParamStringResolver] register.");
        return decryptRequestParamMapResolver;
    }

    @Bean
    @ConditionalOnClass(HttpCryptoProcessor.class)
    @ConditionalOnMissingBean
    public DecryptRequestParamResolver decryptRequestParamResolver(HttpCryptoProcessor httpCryptoProcessor) {
        DecryptRequestParamResolver decryptRequestParamResolver = new DecryptRequestParamResolver();
        decryptRequestParamResolver.setHttpCryptoProcessor(httpCryptoProcessor);
        log.trace("[GOYA] |- component [security] SecurityAutoConfiguration |- bean [decryptRequestParamResolver] register.");
        return decryptRequestParamResolver;
    }

    @Configuration(proxyBeanMethods = false)
    @CryptoStrategy(CryptoStrategyEnum.SM)
    static class SMCryptoConfiguration {

        @PostConstruct
        public void init() {
            log.debug("[GOYA] |- component [web] SMCryptoConfiguration auto configure.");
        }

        @Bean
        @ConditionalOnMissingBean
        public IAsymmetricCryptoProcessor asymmetricCryptoProcessor() {
            Sm2CryptoProcessor sm2CryptoProcessor = new Sm2CryptoProcessor();
            log.trace("[GOYA] |- component [web] SMCryptoConfiguration |- bean [asymmetricCryptoProcessor] register.");
            return sm2CryptoProcessor;
        }

        @Bean
        @ConditionalOnMissingBean
        public ISymmetricCryptoProcessor symmetricCryptoProcessor() {
            Sm4CryptoProcessor sm4CryptoProcessor = new Sm4CryptoProcessor();
            log.trace("[GOYA] |- component [web] SMCryptoConfiguration |- bean [symmetricCryptoProcessor] register.");
            return sm4CryptoProcessor;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @CryptoStrategy(CryptoStrategyEnum.STANDARD)
    static class StandardCryptoConfiguration {

        @PostConstruct
        public void init() {
            log.debug("[GOYA] |- component [web] StandardCryptoConfiguration auto configure.");
        }

        @Bean
        @ConditionalOnMissingBean
        public IAsymmetricCryptoProcessor asymmetricCryptoProcessor() {
            RsaCryptoProcessor rsaCryptoProcessor = new RsaCryptoProcessor();
            log.trace("[GOYA] |- component [web] StandardCryptoConfiguration |- bean [asymmetricCryptoProcessor] register.");
            return rsaCryptoProcessor;
        }

        @Bean
        @ConditionalOnMissingBean
        public ISymmetricCryptoProcessor symmetricCryptoProcessor() {
            AesCryptoProcessor aesCryptoProcessor = new AesCryptoProcessor();
            log.trace("[GOYA] |- component [web] StandardCryptoConfiguration |- bean [symmetricCryptoProcessor] register.");
            return aesCryptoProcessor;
        }
    }
}
