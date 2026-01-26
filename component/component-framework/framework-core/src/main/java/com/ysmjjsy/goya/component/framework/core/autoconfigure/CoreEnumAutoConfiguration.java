package com.ysmjjsy.goya.component.framework.core.autoconfigure;

import com.fasterxml.jackson.databind.Module;
import com.ysmjjsy.goya.component.framework.core.enums.CodeEnumJacksonModule;
import com.ysmjjsy.goya.component.framework.core.enums.dict.CodeEnumClasspathScanner;
import com.ysmjjsy.goya.component.framework.core.enums.dict.DefaultEnumDictionaryService;
import com.ysmjjsy.goya.component.framework.core.enums.dict.EnumDictionaryRegistry;
import com.ysmjjsy.goya.component.framework.core.enums.dict.EnumDictionaryService;
import com.ysmjjsy.goya.component.framework.core.i18n.I18nResolver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Set;

/**
 * <p>枚举体系自动装配</p>
 *
 * @author goya
 * @since 2026/1/24 15:25
 */
@Slf4j
@AutoConfiguration
public class CoreEnumAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] CoreEnumAutoConfiguration auto configure.");
    }

    /**
     * 注册 CodeEnum Jackson Module。
     *
     * @return Module
     */
    @Bean
    public Module codeEnumJacksonModule() {
        Module module = CodeEnumJacksonModule.create();
        log.trace("[Goya] |- component [framework] CoreI18nAutoConfiguration |- bean [codeEnumJacksonModule] register.");
        return module;
    }


    /**
     * CodeEnum classpath 扫描器。
     *
     * @param ctx ApplicationContext
     * @return scanner
     */
    @Bean
    @ConditionalOnMissingBean
    public CodeEnumClasspathScanner codeEnumClasspathScanner(ApplicationContext ctx) {
        CodeEnumClasspathScanner scanner = new CodeEnumClasspathScanner(ctx);
        log.trace("[Goya] |- component [framework] CoreI18nAutoConfiguration |- bean [codeEnumClasspathScanner] register.");
        return scanner;
    }

    /**
     * 枚举字典注册表（由扫描器构建）。
     *
     * @param scanner 扫描器
     * @return registry
     */
    @Bean
    @ConditionalOnMissingBean
    public EnumDictionaryRegistry enumDictionaryRegistry(CodeEnumClasspathScanner scanner) {
        Set<Class<? extends Enum<?>>> enums = scanner.scan();
        EnumDictionaryRegistry registry = new EnumDictionaryRegistry(enums);
        log.trace("[Goya] |- component [framework] CoreI18nAutoConfiguration |- bean [enumDictionaryRegistry] register.");
        return registry;
    }

    /**
     * 枚举字典服务（i18n + 缓存）。
     *
     * @param registry     registry
     * @param i18nResolver i18nResolver
     * @return service
     */
    @Bean
    @ConditionalOnMissingBean
    public EnumDictionaryService enumDictionaryService(EnumDictionaryRegistry registry,
                                                       I18nResolver i18nResolver) {
        DefaultEnumDictionaryService service = new DefaultEnumDictionaryService(registry, i18nResolver);
        log.trace("[Goya] |- component [framework] CoreI18nAutoConfiguration |- bean [enumDictionaryService] register.");
        return service;
    }
}
