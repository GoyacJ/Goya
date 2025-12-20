package com.ysmjjsy.goya.component.captcha.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

/**
 * <p>captcha configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
public class CaptchaAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [captcha] CaptchaAutoConfiguration auto configure.");
    }


}
