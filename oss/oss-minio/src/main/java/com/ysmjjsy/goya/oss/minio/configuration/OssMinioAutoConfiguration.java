package com.ysmjjsy.goya.oss.minio.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>oss minio configuration</p>
 *
 * @author goya
 * @since 2025/12/19 21:28
 */
@Slf4j
@AutoConfiguration
public class OssMinioAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- oss [minio] OssMinioAutoConfiguration auto configure.");
    }
}
