package com.ysmjjsy.goya.component.framework.configuration;

import com.ysmjjsy.goya.component.core.crypto.*;
import com.ysmjjsy.goya.component.framework.annotation.EnvCryptoStrategy;
import com.ysmjjsy.goya.component.framework.configuration.properties.CryptoProperties;
import com.ysmjjsy.goya.component.framework.enums.CryptoStrategyEnum;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/8 23:48
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CryptoProperties.class)
public class CryptoAutoConfiguration {
    
    @PostConstruct
    public void init() {
        log.debug("[Goya] |- framework [framework] CryptoAutoConfiguration auto configure.");
    }

    @Configuration(proxyBeanMethods = false)
    @EnvCryptoStrategy(CryptoStrategyEnum.SM)
    static class SMCryptoConfiguration {

        @PostConstruct
        public void init() {
            log.debug("[Goya] |- framework [framework] SMCryptoConfiguration auto configure.");
        }

        @Bean
        @ConditionalOnMissingBean
        public AsymmetricCryptoProcessor asymmetricCryptoProcessor() {
            Sm2CryptoProcessor sm2CryptoProcessor = new Sm2CryptoProcessor();
            log.trace("[Goya] |- framework [framework] SMCryptoConfiguration |- bean [asymmetricCryptoProcessor] register.");
            return sm2CryptoProcessor;
        }

        @Bean
        @ConditionalOnMissingBean
        public SymmetricCryptoProcessor symmetricCryptoProcessor() {
            Sm4CryptoProcessor sm4CryptoProcessor = new Sm4CryptoProcessor();
            log.trace("[Goya] |- framework [framework] SMCryptoConfiguration |- bean [symmetricCryptoProcessor] register.");
            return sm4CryptoProcessor;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnvCryptoStrategy(CryptoStrategyEnum.STANDARD)
    static class StandardCryptoConfiguration {

        @PostConstruct
        public void init() {
            log.debug("[Goya] |- framework [framework] StandardCryptoConfiguration auto configure.");
        }

        @Bean
        @ConditionalOnMissingBean
        public AsymmetricCryptoProcessor asymmetricCryptoProcessor() {
           RsaCryptoProcessor rsaCryptoProcessor = new RsaCryptoProcessor();
            log.trace("[Goya] |- framework [framework] StandardCryptoConfiguration |- bean [asymmetricCryptoProcessor] register.");
            return rsaCryptoProcessor;
        }

        @Bean
        @ConditionalOnMissingBean
        public SymmetricCryptoProcessor symmetricCryptoProcessor() {
            AesCryptoProcessor aesCryptoProcessor = new AesCryptoProcessor();
            log.trace("[Goya] |- framework [framework] StandardCryptoConfiguration |- bean [symmetricCryptoProcessor] register.");
            return aesCryptoProcessor;
        }
    }
}
