package com.ysmjjsy.goya.component.captcha.configuration;

import com.ysmjjsy.goya.component.captcha.api.ICaptchaService;
import com.ysmjjsy.goya.component.captcha.configuration.properties.CaptchaProperties;
import com.ysmjjsy.goya.component.captcha.core.DefaultCaptchaManager;
import com.ysmjjsy.goya.component.captcha.enums.CaptchaCategoryEnum;
import com.ysmjjsy.goya.component.captcha.factory.CaptchaRendererFactory;
import com.ysmjjsy.goya.component.captcha.provider.ResourceProvider;
import com.ysmjjsy.goya.component.captcha.renderer.behavior.JigsawCaptchaRenderer;
import com.ysmjjsy.goya.component.captcha.renderer.behavior.WordClickCaptchaRenderer;
import com.ysmjjsy.goya.component.captcha.renderer.graphic.*;
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
 * @since 2025/9/30 15:16
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(CaptchaProperties.class)
public class CaptchaAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[GOYA] |- common [captcha] CaptchaAutoConfiguration auto configure.");
    }

    @Bean
    public CaptchaRendererFactory captchaRendererFactory() {
        CaptchaRendererFactory factory = new CaptchaRendererFactory();
        log.trace("[GOYA] |- common [captcha] CaptchaAutoConfiguration |- bean [captchaRendererFactory] register.");
        return factory;
    }

    @Bean
    public ICaptchaService iCaptchaService(CaptchaRendererFactory captchaRendererFactory) {
        DefaultCaptchaManager manager = new DefaultCaptchaManager(captchaRendererFactory);
        log.trace("[GOYA] |- common [captcha] CaptchaAutoConfiguration |- bean [iCaptchaService] register.");
        return manager;
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceProvider resourceProvider(CaptchaProperties captchaProperties) {
        ResourceProvider resourceProvider = new ResourceProvider(captchaProperties);
        log.trace("[GOYA] |- common [captcha] CaptchaAutoConfiguration |- bean [resourceProvider] register.");
        return resourceProvider;
    }

    @Configuration(proxyBeanMethods = false)
    static class BehaviorCaptchaConfiguration {

        @PostConstruct
        public void init() {
            log.debug("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration auto configure.");
        }

        @Bean(CaptchaCategoryEnum.JIGSAW_CAPTCHA)
        public JigsawCaptchaRenderer jigsawCaptchaRenderer() {
            JigsawCaptchaRenderer jigsawCaptchaRenderer = new JigsawCaptchaRenderer();
            log.trace("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration |- bean [jigsawCaptchaRenderer] register.");
            return jigsawCaptchaRenderer;
        }

        @Bean(CaptchaCategoryEnum.WORD_CLICK_CAPTCHA)
        public WordClickCaptchaRenderer wordClickCaptchaRenderer() {
            WordClickCaptchaRenderer wordClickCaptchaRenderer = new WordClickCaptchaRenderer();
            log.trace("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration |- bean [wordClickCaptchaRenderer] register.");
            return wordClickCaptchaRenderer;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class GraphicCaptchaConfiguration {

        @PostConstruct
        public void init() {
            log.debug("[GOYA] |- common [captcha] GraphicCaptchaConfiguration auto configure.");
        }

        @Bean(CaptchaCategoryEnum.ARITHMETIC_CAPTCHA)
        public ArithmeticCaptchaRenderer arithmeticCaptchaRenderer() {
            ArithmeticCaptchaRenderer arithmeticCaptchaRenderer = new ArithmeticCaptchaRenderer();
            log.trace("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration |- bean [arithmeticCaptchaRenderer] register.");
            return arithmeticCaptchaRenderer;
        }

        @Bean(CaptchaCategoryEnum.CHINESE_CAPTCHA)
        public ChineseCaptchaRenderer chineseCaptchaRenderer() {
            ChineseCaptchaRenderer chineseCaptchaRenderer = new ChineseCaptchaRenderer();
            log.trace("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration |- bean [chineseCaptchaRenderer] register.");
            return chineseCaptchaRenderer;
        }

        @Bean(CaptchaCategoryEnum.CHINESE_GIF_CAPTCHA)
        public ChineseGifCaptchaRenderer chineseGifCaptchaRenderer() {
            ChineseGifCaptchaRenderer chineseGifCaptchaRenderer = new ChineseGifCaptchaRenderer();
            log.trace("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration |- bean [chineseGifCaptchaRenderer] register.");
            return chineseGifCaptchaRenderer;
        }

        @Bean(CaptchaCategoryEnum.SPEC_GIF_CAPTCHA)
        public SpecGifCaptchaRenderer specGifCaptchaRenderer() {
            SpecGifCaptchaRenderer specGifCaptchaRenderer = new SpecGifCaptchaRenderer();
            log.trace("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration |- bean [specGifCaptchaRenderer] register.");
            return specGifCaptchaRenderer;
        }

        @Bean(CaptchaCategoryEnum.SPEC_CAPTCHA)
        public SpecCaptchaRenderer specCaptchaRenderer() {
            SpecCaptchaRenderer specCaptchaRenderer = new SpecCaptchaRenderer();
            log.trace("[GOYA] |- common [captcha] BehaviorCaptchaConfiguration |- bean [specCaptchaRenderer] register.");
            return specCaptchaRenderer;
        }
    }
}
