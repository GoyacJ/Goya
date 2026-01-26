package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import com.ysmjjsy.goya.component.framework.servlet.scan.IRestMappingHandler;
import com.ysmjjsy.goya.component.framework.servlet.scan.WebRestMappingScanner;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
public class ServletScanAutoConfiguration {

    @PostConstruct
    public void postConstruct() {
        log.debug("[Goya] |- component [framework] ServletScanAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public WebRestMappingScanner webRestMappingScanner(GoyaWebProperties properties,
                                                    ObjectProvider<IRestMappingHandler> handlerObjectProvider,
                                                    GoyaContext goyaContext) {
        WebRestMappingScanner scanner = new WebRestMappingScanner(properties, handlerObjectProvider, goyaContext);
        log.trace("[Goya] |- component [framework] ServletScanAutoConfiguration |- bean [webRestMappingScanner] register.");
        return scanner;
    }
}
