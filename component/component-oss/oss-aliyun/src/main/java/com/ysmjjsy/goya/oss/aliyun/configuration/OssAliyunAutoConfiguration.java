package com.ysmjjsy.goya.oss.aliyun.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>oss aliyun configuration</p>
 *
 * @author goya
 * @since 2025/12/19 21:28
 */
@Slf4j
@AutoConfiguration
public class OssAliyunAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- oss [aliyun] OssAliyunAutoConfiguration auto configure.");
    }
}
