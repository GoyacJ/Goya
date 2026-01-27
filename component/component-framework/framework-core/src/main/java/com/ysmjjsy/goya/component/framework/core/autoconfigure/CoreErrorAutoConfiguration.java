package com.ysmjjsy.goya.component.framework.core.autoconfigure;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCodeCatalog;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCodeCatalog;
import com.ysmjjsy.goya.component.framework.core.autoconfigure.properties.ErrorGovernanceProperties;
import com.ysmjjsy.goya.component.framework.core.error.DefaultErrorMessageResolver;
import com.ysmjjsy.goya.component.framework.core.error.ErrorMessageResolver;
import com.ysmjjsy.goya.component.framework.core.error.ValidationExceptionTranslator;
import com.ysmjjsy.goya.component.framework.core.error.governance.ErrorCodeGovernanceRunner;
import com.ysmjjsy.goya.component.framework.core.i18n.I18nResolver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 13:47
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({ErrorGovernanceProperties.class})
public class CoreErrorAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] CoreErrorAutoConfiguration auto configure.");
    }

    /**
     * 默认错误消息解析器。
     *
     * @param i18nResolver I18nResolver
     * @return ErrorMessageResolver
     */
    @Bean
    @ConditionalOnMissingBean(ErrorMessageResolver.class)
    public ErrorMessageResolver errorMessageResolver(I18nResolver i18nResolver) {
        DefaultErrorMessageResolver resolver = new DefaultErrorMessageResolver(i18nResolver);
        log.trace("[Goya] |- component [framework] CoreErrorAutoConfiguration |- bean [errorMessageResolver] register.");
        return resolver;
    }

    /**
     * Jakarta Validation 异常转换器（非 Web 场景使用）。
     *
     * @return ValidationExceptionTranslator
     */
    @Bean
    public ValidationExceptionTranslator validationExceptionTranslator() {
        ValidationExceptionTranslator translator = new ValidationExceptionTranslator();
        log.trace("[Goya] |- component [framework] CoreErrorAutoConfiguration |- bean [validationExceptionTranslator] register.");
        return translator;
    }

    /**
     * 默认注册 common 的错误码目录。
     *
     * <p>业务模块应自行提供更多 {@link ErrorCodeCatalog} Bean。</p>
     *
     * @return ErrorCodeCatalog
     */
    @Bean
    @ConditionalOnMissingBean(name = "commonErrorCodeCatalog")
    public ErrorCodeCatalog commonErrorCodeCatalog() {
        CommonErrorCodeCatalog commonErrorCodeCatalog = new CommonErrorCodeCatalog();
        log.trace("[Goya] |- component [framework] CoreErrorAutoConfiguration |- bean [commonErrorCodeCatalog] register.");
        return commonErrorCodeCatalog;
    }

    /**
     * 错误码治理执行器（启动校验）。
     *
     * @param catalogs 所有目录
     * @param props 治理配置项
     * @return runner
     */
    @Bean
    @ConditionalOnMissingBean
    public ErrorCodeGovernanceRunner errorCodeGovernanceRunner(List<ErrorCodeCatalog> catalogs,
                                                               ErrorGovernanceProperties props) {
        ErrorCodeGovernanceRunner errorCodeGovernanceRunner = new ErrorCodeGovernanceRunner(catalogs, props);
        log.trace("[Goya] |- component [framework] CoreErrorAutoConfiguration |- bean [errorCodeGovernanceRunner] register.");
        return errorCodeGovernanceRunner;
    }
}
