package com.ysmjjsy.goya.component.oss.aliyun.configuration;

import com.ysmjjsy.goya.component.framework.oss.annotation.ConditionalOnOssStrategy;
import com.ysmjjsy.goya.component.framework.oss.enums.OssEnum;
import com.ysmjjsy.goya.component.oss.aliyun.configuration.properties.AliyunProperties;
import com.ysmjjsy.goya.component.oss.aliyun.definition.pool.AliyunClientObjectPool;
import com.ysmjjsy.goya.component.oss.aliyun.definition.pool.AliyunClientPooledObjectFactory;
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
 * @since 2025/11/1 17:17
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(AliyunProperties.class)
@ComponentScan(basePackages = {
        "com.ysmjjsy.goya.component.oss.aliyun.service",
        "com.ysmjjsy.goya.component.oss.aliyun.repository",
})
@ConditionalOnOssStrategy(OssEnum.ALIYUN)
public class OssAliyunAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- openapi [aliyun] OssAliyunAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public AliyunClientObjectPool aliyunClientObjectPool(AliyunProperties aliyunProperties) {
        AliyunClientPooledObjectFactory factory = new AliyunClientPooledObjectFactory(aliyunProperties);
        AliyunClientObjectPool pool = new AliyunClientObjectPool(factory);
        log.trace("[Goya] |- Bean [Aliyun Client Pool] Configure.");
        return pool;
    }
}
