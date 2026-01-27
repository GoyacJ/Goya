package com.ysmjjsy.goya.component.framework.log.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.web.RequestInfoExtractor;
import com.ysmjjsy.goya.component.framework.log.aop.LogAspect;
import com.ysmjjsy.goya.component.framework.log.autoconfigure.properties.LogProperties;
import com.ysmjjsy.goya.component.framework.log.filter.TraceMdcFilter;
import com.ysmjjsy.goya.component.framework.log.mask.MethodArgMasker;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import io.micrometer.tracing.Tracer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>自动装配</p>
 * <ul>
 *   <li>Masker（默认 DefaultMasker）</li>
 *   <li>LogAspect（方法日志切面）</li>
 *   <li>MaskingKeyClassifier（按 key 分类敏感字段）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 22:08
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(LogProperties.class)
public class LogAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] LogAutoConfiguration auto configure.");
    }

    /**
     * 参数脱敏器。
     *
     * @param masker 默认脱敏器
     * @return masker
     */
    @Bean
    @ConditionalOnMissingBean(Masker.class)
    public MethodArgMasker methodArgMasker(Masker masker) {
        MethodArgMasker methodArgMasker = new MethodArgMasker(masker);
        log.trace("[Goya] |- component [framework] LogAutoConfiguration |- bean [methodArgMasker] register.");
        return methodArgMasker;
    }

    /**
     * 方法日志切面。
     *
     * @param props  配置
     * @param masker 脱敏器
     * @return aspect
     */
    @Bean
    @ConditionalOnMissingBean
    public LogAspect logAspect(LogProperties props, Masker masker, MethodArgMasker methodArgMasker, RequestInfoExtractor requestInfoExtractor) {
        LogAspect logAspect = new LogAspect(props, masker, methodArgMasker, requestInfoExtractor);
        log.trace("[Goya] |- component [framework] LogAutoConfiguration |- bean [logAspect] register.");
        return logAspect;
    }


    /**
     * 注册 TraceMdcFilter。
     *
     * @return filter
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceMdcFilter traceMdcFilter(@Autowired(required = false) Tracer tracer) {
        TraceMdcFilter traceMdcFilter = new TraceMdcFilter(tracer);
        log.trace("[Goya] |- component [framework] LogAutoConfiguration |- bean [traceMdcFilter] register.");
        return traceMdcFilter;
    }
}
