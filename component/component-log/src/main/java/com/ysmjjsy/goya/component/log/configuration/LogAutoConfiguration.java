package com.ysmjjsy.goya.component.log.configuration;

import com.ysmjjsy.goya.component.bus.stream.service.IBusService;
import com.ysmjjsy.goya.component.log.aspect.GoyaLogAspect;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 21:19
 */
@Slf4j
@AutoConfiguration
public class LogAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [log] LogAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(GoyaLogAspect.class)
    public GoyaLogAspect goyaLogAspect(IBusService iBus) {
        GoyaLogAspect goyaLogAspect = new GoyaLogAspect(iBus);
        log.trace("[Goya] |- component [catchlog] |- bean [goyaLogAspect] register.");
        return goyaLogAspect;
    }
}
