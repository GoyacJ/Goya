package com.ysmjjsy.goya.component.oss.s3.configuration;

import com.ysmjjsy.goya.component.framework.oss.annotation.ConditionalOnOssStrategy;
import com.ysmjjsy.goya.component.framework.oss.enums.OssEnum;
import com.ysmjjsy.goya.component.oss.s3.configuration.properties.S3Properties;
import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientObjectPool;
import com.ysmjjsy.goya.component.oss.s3.definition.pool.S3ClientPooledObjectFactory;
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
 * @since 2025/11/1 17:36
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(S3Properties.class)
@ComponentScan(basePackages = {
        "com.ysmjjsy.goya.component.oss.s3.service",
        "com.ysmjjsy.goya.component.oss.s3.repository",
})
@ConditionalOnOssStrategy(OssEnum.S3)
public class OssS3AutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- openapi [s3] OssS3AutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public S3ClientObjectPool s3ClientObjectPool(S3Properties s3Properties) {
        S3ClientPooledObjectFactory factory = new S3ClientPooledObjectFactory(s3Properties);
        S3ClientObjectPool pool = new S3ClientObjectPool(factory);
        log.trace("[Goya] |- Bean [S3 Client Pool] Configure.");
        return pool;
    }
}
