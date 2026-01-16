package com.ysmjjsy.goya.component.web.configuration;

import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.web.advice.DecryptRequestBodyAdvice;
import com.ysmjjsy.goya.component.web.advice.EncryptResponseBodyAdvice;
import com.ysmjjsy.goya.component.web.cache.AccessLimitedCacheManager;
import com.ysmjjsy.goya.component.web.cache.IdempotentCacheManager;
import com.ysmjjsy.goya.component.web.configuration.properties.WebProperties;
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

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 09:58
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class HttpCryptoAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[Goya] |- component [web] CryptoAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentCacheManager idempotentCacheManager(WebProperties webProperties) {
        IdempotentCacheManager idempotentStampManager = new IdempotentCacheManager(webProperties.idempotent());
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [idempotentStampManager] register.");
        return idempotentStampManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public AccessLimitedCacheManager accessLimitedCacheManager(WebProperties webProperties) {
        AccessLimitedCacheManager accessLimitedStampManager = new AccessLimitedCacheManager(webProperties.accessLimited());
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [accessLimitedStampManager] register.");
        return accessLimitedStampManager;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(IdempotentCacheManager.class)
    public IdempotentInterceptor idempotentInterceptor(IdempotentCacheManager idempotentCacheManager) {
        IdempotentInterceptor interceptor = new IdempotentInterceptor(idempotentCacheManager);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [idempotentInterceptor] register.");
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(AccessLimitedCacheManager.class)
    public AccessLimitedInterceptor accessLimitedInterceptor(AccessLimitedCacheManager accessLimitedCacheManager) {
        AccessLimitedInterceptor interceptor = new AccessLimitedInterceptor(accessLimitedCacheManager);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [accessLimitedInterceptor] register.");
        return interceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public XssHttpServletFilter xssHttpServletFilter() {
        XssHttpServletFilter filter = new XssHttpServletFilter();
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [xssHttpServletFilter] register.");
        return filter;
    }

    @Bean
    @ConditionalOnClass(CryptoProcessor.class)
    @ConditionalOnMissingBean
    public DecryptRequestBodyAdvice decryptRequestBodyAdvice(CryptoProcessor httpCryptoProcessor) {
        DecryptRequestBodyAdvice decryptRequestBodyAdvice = new DecryptRequestBodyAdvice();
        decryptRequestBodyAdvice.setInterfaceCryptoProcessor(httpCryptoProcessor);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [decryptRequestBodyAdvice] register.");
        return decryptRequestBodyAdvice;
    }

    @Bean
    @ConditionalOnClass(CryptoProcessor.class)
    @ConditionalOnMissingBean
    public EncryptResponseBodyAdvice encryptResponseBodyAdvice(CryptoProcessor httpCryptoProcessor) {
        EncryptResponseBodyAdvice encryptResponseBodyAdvice = new EncryptResponseBodyAdvice(httpCryptoProcessor);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [encryptResponseBodyAdvice] register.");
        return encryptResponseBodyAdvice;
    }

    @Bean
    @ConditionalOnClass(CryptoProcessor.class)
    @ConditionalOnMissingBean
    public DecryptRequestParamMapResolver decryptRequestParamStringResolver(CryptoProcessor httpCryptoProcessor) {
        DecryptRequestParamMapResolver decryptRequestParamMapResolver = new DecryptRequestParamMapResolver();
        decryptRequestParamMapResolver.setCryptoProcessor(httpCryptoProcessor);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [decryptRequestParamStringResolver] register.");
        return decryptRequestParamMapResolver;
    }

    @Bean
    @ConditionalOnClass(CryptoProcessor.class)
    @ConditionalOnMissingBean
    public DecryptRequestParamResolver decryptRequestParamResolver(CryptoProcessor httpCryptoProcessor) {
        DecryptRequestParamResolver decryptRequestParamResolver = new DecryptRequestParamResolver();
        decryptRequestParamResolver.setCryptoProcessor(httpCryptoProcessor);
        log.trace("[Goya] |- component [security] SecurityAutoConfiguration |- bean [decryptRequestParamResolver] register.");
        return decryptRequestParamResolver;
    }
}
