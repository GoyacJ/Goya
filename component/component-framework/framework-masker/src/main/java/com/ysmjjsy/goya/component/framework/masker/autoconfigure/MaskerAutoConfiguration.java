package com.ysmjjsy.goya.component.framework.masker.autoconfigure;

import com.ysmjjsy.goya.component.framework.masker.autoconfigure.properties.MaskerProperties;
import com.ysmjjsy.goya.component.framework.masker.core.DefaultMasker;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import com.ysmjjsy.goya.component.framework.masker.core.MaskingKeyClassifier;
import com.ysmjjsy.goya.component.framework.masker.response.ApiResponseMaskingAdvice;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>自动装配</p>
 *
 * @author goya
 * @since 2026/1/24 22:57
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MaskerProperties.class)
public class MaskerAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] MaskerAutoConfiguration auto configure.");
    }


    /**
     * key 分类器。
     *
     * @param props 配置项
     * @return classifier
     */
    @Bean
    @ConditionalOnMissingBean
    public MaskingKeyClassifier maskingKeyClassifier(MaskerProperties props) {
        MaskingKeyClassifier maskingKeyClassifier = new MaskingKeyClassifier(props.extraSensitiveKeys());
        log.trace("[Goya] |- component [framework] MaskerAutoConfiguration |- bean [maskingKeyClassifier] register.");
        return maskingKeyClassifier;
    }

    /**
     * 默认脱敏器。
     *
     * @param props 配置项
     * @param classifier 分类器
     * @return masker
     */
    @Bean
    @ConditionalOnMissingBean(Masker.class)
    public Masker defaultMasker(MaskerProperties props, MaskingKeyClassifier classifier) {
        DefaultMasker defaultMasker = new DefaultMasker(props, classifier);
        log.trace("[Goya] |- component [framework] MaskerAutoConfiguration |- bean [defaultMasker] register.");
        return defaultMasker;
    }

    /**
     * 注册 ApiResponse 脱敏 Advice。
     *
     * @param properties 配置项
     * @param masker 脱敏器
     * @return advice
     */
    @Bean
    public ApiResponseMaskingAdvice apiResponseMaskingAdvice(MaskerProperties properties, Masker masker) {
        ApiResponseMaskingAdvice apiResponseMaskingAdvice = new ApiResponseMaskingAdvice(properties, masker);
        log.trace("[Goya] |- component [framework] MaskerAutoConfiguration |- bean [apiResponseMaskingAdvice] register.");
        return apiResponseMaskingAdvice;
    }
}
