package com.ysmjjsy.goya.component.oss.minio.configuration;

import com.ysmjjsy.goya.component.oss.minio.configuration.properties.MinioProperties;
import com.ysmjjsy.goya.component.oss.minio.definition.pool.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/11/1 17:04
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(MinioProperties.class)
@ComponentScan(basePackages = {
        "com.ysmjjsy.goya.component.oss.minio.service",
        "com.ysmjjsy.goya.component.oss.minio.repository",
})
public class OssMinioAutoConfiguration {
    
    @PostConstruct
    public void init() {
        log.debug("[Goya] |- openapi [minio] OssMinioAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioClientObjectPool minioClientPool(MinioProperties minioProperties) {
        MinioClientPooledObjectFactory factory = new MinioClientPooledObjectFactory(minioProperties);
        MinioClientObjectPool pool = new MinioClientObjectPool(factory);
        log.trace("[Goya] |- Bean [Minio Client Pool] Configure.");
        return pool;
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioAsyncClientObjectPool minioAsyncClientPool(MinioProperties minioProperties) {
        MinioAsyncClientPooledObjectFactory factory = new MinioAsyncClientPooledObjectFactory(minioProperties);
        MinioAsyncClientObjectPool pool = new MinioAsyncClientObjectPool(factory);
        log.trace("[Goya] |- Bean [Minio Async Client Pool] Configure.");
        return pool;
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioAdminClientObjectPool minioAdminClientPool(MinioProperties minioProperties) {
        MinioAdminClientPooledObjectFactory factory = new MinioAdminClientPooledObjectFactory(minioProperties);
        MinioAdminClientObjectPool pool = new MinioAdminClientObjectPool(factory);
        log.trace("[Goya] |- Bean [Minio Admin Client Pool] Configure.");
        return pool;
    }
}
