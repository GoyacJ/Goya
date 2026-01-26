package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.error.ErrorMessageResolver;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.ServletErrorProperties;
import com.ysmjjsy.goya.component.framework.servlet.web.*;
import io.micrometer.tracing.Tracer;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>Servlet Web 错误体系自动装配</p>
 *
 * @author goya
 * @since 2026/1/24 14:02
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@EnableConfigurationProperties(ServletErrorProperties.class)
@Slf4j
public class ServletErrorAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] ServletErrorAutoConfiguration auto configure.");
    }

    /**
     * 默认 HTTP 状态码映射器。
     *
     * @param props 配置项
     * @return HttpStatusMapper
     */
    @Bean
    @ConditionalOnMissingBean
    public HttpStatusMapper httpStatusMapper(ServletErrorProperties props) {
        DefaultHttpStatusMapper mapper = new DefaultHttpStatusMapper(props.bizUseHttp200());
        log.trace("[Goya] |- component [framework] ServletErrorAutoConfiguration |- bean [httpStatusMapper] register.");
        return mapper;
    }

    /**
     * 默认 TraceIdResolver。
     *
     * @param tracer Micrometer Tracer（可为空）
     * @param props  配置项
     * @return TraceIdResolver
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceIdResolver traceIdResolver(@Autowired(required = false) Tracer tracer, ServletErrorProperties props) {
        DefaultTraceIdResolver defaultTraceIdResolver = new DefaultTraceIdResolver(tracer, props.traceHeaderName());
        log.trace("[Goya] |- component [framework] ServletErrorAutoConfiguration |- bean [traceIdResolver] register.");
        return defaultTraceIdResolver;
    }

    /**
     * ProblemDetail 工厂。
     *
     * @param messageResolver 消息解析器（framework-core）
     * @param statusMapper    状态码映射器
     * @param props           配置项
     * @return ProblemDetailFactory
     */
    @Bean
    @ConditionalOnMissingBean
    public ProblemDetailFactory problemDetailFactory(ErrorMessageResolver messageResolver,
                                                     HttpStatusMapper statusMapper,
                                                     ServletErrorProperties props) {
        ProblemDetailFactory problemDetailFactory = new ProblemDetailFactory(messageResolver, statusMapper, props);
        log.trace("[Goya] |- component [framework] ServletErrorAutoConfiguration |- bean [problemDetailFactory] register.");
        return problemDetailFactory;
    }

    /**
     * 全局异常处理器。
     *
     * @param messageResolver 消息解析器（framework-core）
     * @param statusMapper    状态码映射器
     * @param traceIdResolver traceId 解析器
     * @param props           配置项
     * @param problemFactory  ProblemDetail 工厂
     * @param masker          脱敏器（framework-core）
     * @return GlobalExceptionHandler
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ErrorMessageResolver messageResolver,
                                                         HttpStatusMapper statusMapper,
                                                         TraceIdResolver traceIdResolver,
                                                         ServletErrorProperties props,
                                                         ProblemDetailFactory problemFactory,
                                                         Masker masker) {
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler(messageResolver, statusMapper, traceIdResolver, props, problemFactory, masker);
        log.trace("[Goya] |- component [framework] ServletErrorAutoConfiguration |- bean [globalExceptionHandler] register.");
        return globalExceptionHandler;
    }

    @Bean
    public TraceApiResponseAdvice traceApiResponseAdvice(@Autowired(required = false) Tracer tracer) {
        TraceApiResponseAdvice traceApiResponseAdvice = new TraceApiResponseAdvice(tracer);
        log.trace("[Goya] |- component [framework] ServletErrorAutoConfiguration |- bean [traceApiResponseAdvice] register.");
        return traceApiResponseAdvice;
    }
}
