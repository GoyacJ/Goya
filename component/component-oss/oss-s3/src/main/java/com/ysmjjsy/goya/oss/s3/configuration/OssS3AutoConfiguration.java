package com.ysmjjsy.goya.oss.s3.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>oss s3 configuration</p>
 *
 * @author goya
 * @since 2025/12/19 21:28
 */
@Slf4j
@AutoConfiguration
public class OssS3AutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- oss [s3] OssS3AutoConfiguration auto configure.");
    }
}
