package com.ysmjjsy.goya.component.social.configuration;

import com.ysmjjsy.goya.component.framework.strategy.StrategyChoose;
import com.ysmjjsy.goya.component.social.cache.SmsCheckCacheManager;
import com.ysmjjsy.goya.component.social.cache.ThirdPartCheckCacheManager;
import com.ysmjjsy.goya.component.social.configuration.properties.SocialProperties;
import com.ysmjjsy.goya.component.social.service.SmsService;
import com.ysmjjsy.goya.component.social.service.SocialService;
import com.ysmjjsy.goya.component.social.service.ThirdPartService;
import com.ysmjjsy.goya.component.social.service.WxMiniProgramService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <p>social configuration</p>
 *
 * @author goya
 * @since 2025/12/19 17:29
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SocialProperties.class)
public class SocialAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [social] SocialAutoConfiguration auto configure.");
    }

    @Bean
    public SmsCheckCacheManager smsCheckCacheManager(SocialProperties socialProperties) {
        SmsCheckCacheManager manager = new SmsCheckCacheManager(socialProperties.sms());
        log.trace("[Goya] |- component [social] SocialAutoConfiguration |- bean [smsCheckCacheManager] register.");
        return manager;
    }

    @Bean
    public ThirdPartCheckCacheManager thirdPartCheckCacheManager(SocialProperties socialProperties) {
        ThirdPartCheckCacheManager manager = new ThirdPartCheckCacheManager(socialProperties.thirdPart());
        log.trace("[Goya] |- component [social] SocialAutoConfiguration |- bean [thirdPartCheckCacheManager] register.");
        return manager;
    }

    @Bean
    public SmsService smsService(SmsCheckCacheManager smsCheckCacheManager) {
        SmsService smsService = new SmsService(smsCheckCacheManager);
        log.trace("[Goya] |- component [social] SocialAutoConfiguration |- bean [smsService] register.");
        return smsService;
    }

    @Bean
    public ThirdPartService thirdPartService(ThirdPartCheckCacheManager thirdPartCheckCacheManager) {
        ThirdPartService thirdService = new ThirdPartService(thirdPartCheckCacheManager);
        log.trace("[Goya] |- component [social] SocialAutoConfiguration |- bean [thirdPartService] register.");
        return thirdService;
    }

    @Bean
    public WxMiniProgramService wxMiniProgramService(SocialProperties socialProperties) {
        WxMiniProgramService wxMiniProgramService = new WxMiniProgramService(socialProperties.wxMiniProgram());
        log.trace("[Goya] |- component [social] SocialAutoConfiguration |- bean [wxMiniProgramService] register.");
        return wxMiniProgramService;
    }

    @Bean
    public SocialService socialService(StrategyChoose strategyChoose, SmsService smsService, ThirdPartService thirdPartService, WxMiniProgramService wxMiniProgramService) {
        SocialService socialService = new SocialService(strategyChoose, smsService, thirdPartService, wxMiniProgramService);
        log.trace("[Goya] |- component [social] SocialAutoConfiguration |- bean [socialService] register.");
        return socialService;
    }

}
