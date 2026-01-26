package com.ysmjjsy.goya.component.framework.servlet.autoconfigure;

import com.ysmjjsy.goya.component.framework.core.enums.dict.EnumDictionaryService;
import com.ysmjjsy.goya.component.framework.servlet.enums.EnumDictionaryController;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 15:55
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@Slf4j
public class ServletEnumAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [framework] ServletEnumAutoConfiguration auto configure.");
    }

    /**
     * 注册枚举字典控制器。
     *
     * @param service 字典服务
     * @return controller
     */
    @Bean
    public EnumDictionaryController enumDictionaryController(EnumDictionaryService service) {
        EnumDictionaryController controller = new EnumDictionaryController(service);
        log.trace("[Goya] |- component [framework] ServletEnumAutoConfiguration |- bean [enumDictionaryController] register.");
        return controller;
    }
}
