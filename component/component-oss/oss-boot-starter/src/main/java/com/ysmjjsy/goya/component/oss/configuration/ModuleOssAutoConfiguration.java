package com.ysmjjsy.goya.component.oss.configuration;

import com.ysmjjsy.goya.component.oss.aliyun.configuration.OssAliyunAutoConfiguration;
import com.ysmjjsy.goya.component.oss.configuration.properties.OssProperties;
import com.ysmjjsy.goya.component.oss.core.annotation.ConditionalOnOssStrategy;
import com.ysmjjsy.goya.component.oss.core.enums.OssEnum;
import com.ysmjjsy.goya.component.oss.minio.configuration.OssMinioAutoConfiguration;
import com.ysmjjsy.goya.component.oss.proxy.OssPresignedUrlProxy;
import com.ysmjjsy.goya.component.oss.s3.configuration.OssS3AutoConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

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
        "com.ysmjjsy.goya.component.oss.service",
        "com.ysmjjsy.goya.component.oss.controller"
})
public class ModuleOssAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[HZ-ZHG] |- module [oss] ModuleOssAutoConfiguration auto configure.");
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnOssStrategy(value = OssEnum.ALIYUN)
    @Import({
            OssAliyunAutoConfiguration.class
    })
    static class UserAliyunConfiguration {

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnOssStrategy(value = OssEnum.MINIO)
    @Import({
            OssMinioAutoConfiguration.class,
    })
    @ComponentScan(basePackages = {
            "com.ysmjjsy.goya.component.oss.minio.service",
            "com.ysmjjsy.goya.component.oss.minio.controller",
    })
    static class UserMinioConfiguration {

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnOssStrategy(value = OssEnum.S3)
    @Import({
            OssS3AutoConfiguration.class,
    })
    static class UserS3Configuration {

    }

    @Bean
    public OssPresignedUrlProxy ossPresignedUrlProxy(OssProperties ossProperties) {
        OssPresignedUrlProxy ossPresignedUrlProxy = new OssPresignedUrlProxy(ossProperties);
        log.trace("[HZ-ZHG] |- Bean [Oss Presigned Url Proxy] Configure.");
        return ossPresignedUrlProxy;
    }
}
