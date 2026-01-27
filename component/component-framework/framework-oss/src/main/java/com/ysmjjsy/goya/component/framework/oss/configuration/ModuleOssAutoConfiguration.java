package com.ysmjjsy.goya.component.framework.oss.configuration;

import com.ysmjjsy.goya.component.framework.oss.configuration.properties.OssProperties;
import com.ysmjjsy.goya.component.framework.oss.proxy.OssPresignedUrlProxy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/1 17:51
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(OssProperties.class)
@ComponentScan(basePackages = {
        "com.ysmjjsy.goya.component.framework.oss.service",
        "com.ysmjjsy.goya.component.framework.oss.controller"
})
public class ModuleOssAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- module [oss] ModuleOssAutoConfiguration auto configure.");
    }


    @Bean
    public OssPresignedUrlProxy ossPresignedUrlProxy(OssProperties ossProperties) {
        OssPresignedUrlProxy ossPresignedUrlProxy = new OssPresignedUrlProxy(ossProperties);
        log.trace("[Goya] |- Bean [Oss Presigned Url Proxy] Configure.");
        return ossPresignedUrlProxy;
    }
}
