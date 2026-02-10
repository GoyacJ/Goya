package com.ysmjjsy.goya.component.security.authentication.configuration;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.security.authentication.auth.*;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.controller.SecurityAuthController;
import com.ysmjjsy.goya.component.security.authentication.controller.SecurityLoginViewController;
import com.ysmjjsy.goya.component.security.authentication.service.*;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.service.LoginRiskEvaluator;
import com.ysmjjsy.goya.component.security.core.service.ITenantService;
import com.ysmjjsy.goya.component.security.core.service.IOtpService;
import com.ysmjjsy.goya.component.social.service.ThirdPartService;
import com.ysmjjsy.goya.component.social.service.WxMiniProgramService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * <p>认证模块自动配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecurityAuthenticationProperties.class)
@ConditionalOnProperty(prefix = "goya.security.authentication", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(SecurityAuthenticationWebSecurityConfiguration.class)
public class SecurityAuthenticationAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authentication] SecurityAuthenticationAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(LoginRiskEvaluator.class)
    public LoginRiskEvaluator loginRiskEvaluator(SecurityAuthenticationProperties securityAuthenticationProperties) {
        DefaultLoginRiskEvaluator evaluator = new DefaultLoginRiskEvaluator(securityAuthenticationProperties);
        log.trace("[Goya] |- security [authentication] |- bean [loginRiskEvaluator] register.");
        return evaluator;
    }

    @Bean
    public DeviceTrustService deviceTrustService(SecurityUserManager securityUserManager,
                                                 SecurityAuthenticationProperties securityAuthenticationProperties) {
        DeviceTrustService deviceTrustService = new DeviceTrustService(securityUserManager, securityAuthenticationProperties);
        log.trace("[Goya] |- security [authentication] |- bean [deviceTrustService] register.");
        return deviceTrustService;
    }

    @Bean
    public PreAuthCodeService preAuthCodeService(CacheService cacheService,
                                                 SecurityAuthenticationProperties securityAuthenticationProperties) {
        PreAuthCodeService preAuthCodeService = new PreAuthCodeService(cacheService, securityAuthenticationProperties);
        log.trace("[Goya] |- security [authentication] |- bean [preAuthCodeService] register.");
        return preAuthCodeService;
    }

    @Bean
    public RiskService riskService(LoginRiskEvaluator loginRiskEvaluator) {
        RiskService riskService = new RiskService(loginRiskEvaluator);
        log.trace("[Goya] |- security [authentication] |- bean [riskService] register.");
        return riskService;
    }

    @Bean
    public MfaService mfaService(CacheService cacheService,
                                 PreAuthCodeService preAuthCodeService,
                                 ObjectProvider<IOtpService> otpServiceProvider,
                                 SecurityUserManager securityUserManager,
                                 DeviceTrustService deviceTrustService,
                                 SecurityAuthenticationProperties securityAuthenticationProperties) {
        MfaService mfaService = new MfaService(
                cacheService,
                preAuthCodeService,
                otpServiceProvider,
                securityUserManager,
                deviceTrustService,
                securityAuthenticationProperties
        );
        log.trace("[Goya] |- security [authentication] |- bean [mfaService] register.");
        return mfaService;
    }

    @Bean
    public PrimaryAuthIssueService primaryAuthIssueService(SecurityAuthenticationProperties securityAuthenticationProperties,
                                                           RiskService riskService,
                                                           MfaService mfaService,
                                                           PreAuthCodeService preAuthCodeService,
                                                           DeviceTrustService deviceTrustService,
                                                           SecurityUserManager securityUserManager) {
        PrimaryAuthIssueService primaryAuthIssueService = new PrimaryAuthIssueService(
                securityAuthenticationProperties,
                riskService,
                mfaService,
                preAuthCodeService,
                deviceTrustService,
                securityUserManager
        );
        log.trace("[Goya] |- security [authentication] |- bean [primaryAuthIssueService] register.");
        return primaryAuthIssueService;
    }

    @Bean
    public PasswordAuthService passwordAuthService(SecurityUserManager securityUserManager,
                                                   SecurityAuthenticationProperties securityAuthenticationProperties,
                                                   CacheService cacheService,
                                                   ObjectProvider<com.ysmjjsy.goya.component.captcha.api.CaptchaService> captchaServiceProvider,
                                                   ObjectProvider<PasswordEncoder> passwordEncoderProvider,
                                                   ObjectProvider<ITenantService> tenantServiceProvider,
                                                   PrimaryAuthIssueService primaryAuthIssueService) {
        PasswordAuthService passwordAuthService = new PasswordAuthService(
                securityUserManager,
                securityAuthenticationProperties,
                cacheService,
                captchaServiceProvider,
                passwordEncoderProvider,
                tenantServiceProvider,
                primaryAuthIssueService
        );
        log.trace("[Goya] |- security [authentication] |- bean [passwordAuthService] register.");
        return passwordAuthService;
    }

    @Bean
    public SmsAuthService smsAuthService(SecurityUserManager securityUserManager,
                                         ObjectProvider<IOtpService> otpServiceProvider,
                                         PrimaryAuthIssueService primaryAuthIssueService) {
        SmsAuthService smsAuthService = new SmsAuthService(
                securityUserManager,
                otpServiceProvider,
                primaryAuthIssueService
        );
        log.trace("[Goya] |- security [authentication] |- bean [smsAuthService] register.");
        return smsAuthService;
    }

    @Bean
    public SocialAuthService socialAuthService(ObjectProvider<ThirdPartService> thirdPartServiceProvider,
                                               SecurityUserManager securityUserManager,
                                               PrimaryAuthIssueService primaryAuthIssueService) {
        SocialAuthService socialAuthService = new SocialAuthService(
                thirdPartServiceProvider,
                securityUserManager,
                primaryAuthIssueService
        );
        log.trace("[Goya] |- security [authentication] |- bean [socialAuthService] register.");
        return socialAuthService;
    }

    @Bean
    public WxMiniProgramAuthService wxMiniProgramAuthService(ObjectProvider<WxMiniProgramService> wxMiniProgramServiceProvider,
                                                             SecurityUserManager securityUserManager,
                                                             PrimaryAuthIssueService primaryAuthIssueService) {
        WxMiniProgramAuthService wxMiniProgramAuthService = new WxMiniProgramAuthService(
                wxMiniProgramServiceProvider,
                securityUserManager,
                primaryAuthIssueService
        );
        log.trace("[Goya] |- security [authentication] |- bean [wxMiniProgramAuthService] register.");
        return wxMiniProgramAuthService;
    }

    @Bean
    public PasswordAuthenticationProvider passwordAuthenticationProvider(PasswordAuthService passwordAuthService) {
        PasswordAuthenticationProvider provider = new PasswordAuthenticationProvider(passwordAuthService);
        log.trace("[Goya] |- security [authentication] |- bean [passwordAuthenticationProvider] register.");
        return provider;
    }

    @Bean
    public SmsAuthenticationProvider smsAuthenticationProvider(SmsAuthService smsAuthService) {
        SmsAuthenticationProvider provider = new SmsAuthenticationProvider(smsAuthService);
        log.trace("[Goya] |- security [authentication] |- bean [smsAuthenticationProvider] register.");
        return provider;
    }

    @Bean
    public SocialAuthenticationProvider socialAuthenticationProvider(SocialAuthService socialAuthService) {
        SocialAuthenticationProvider provider = new SocialAuthenticationProvider(socialAuthService);
        log.trace("[Goya] |- security [authentication] |- bean [socialAuthenticationProvider] register.");
        return provider;
    }

    @Bean
    public WxMiniProgramAuthenticationProvider wxMiniProgramAuthenticationProvider(WxMiniProgramAuthService wxMiniProgramAuthService) {
        WxMiniProgramAuthenticationProvider provider = new WxMiniProgramAuthenticationProvider(wxMiniProgramAuthService);
        log.trace("[Goya] |- security [authentication] |- bean [wxMiniProgramAuthenticationProvider] register.");
        return provider;
    }

    @Bean
    public MfaVerifyAuthenticationProvider mfaVerifyAuthenticationProvider(MfaService mfaService) {
        MfaVerifyAuthenticationProvider provider = new MfaVerifyAuthenticationProvider(mfaService);
        log.trace("[Goya] |- security [authentication] |- bean [mfaVerifyAuthenticationProvider] register.");
        return provider;
    }

    @Bean(name = "securityAuthenticationManager")
    public AuthenticationManager securityAuthenticationManager(PasswordAuthenticationProvider passwordAuthenticationProvider,
                                                               SmsAuthenticationProvider smsAuthenticationProvider,
                                                               SocialAuthenticationProvider socialAuthenticationProvider,
                                                               WxMiniProgramAuthenticationProvider wxMiniProgramAuthenticationProvider,
                                                               MfaVerifyAuthenticationProvider mfaVerifyAuthenticationProvider) {
        ProviderManager providerManager = new ProviderManager(List.<AuthenticationProvider>of(
                passwordAuthenticationProvider,
                smsAuthenticationProvider,
                socialAuthenticationProvider,
                wxMiniProgramAuthenticationProvider,
                mfaVerifyAuthenticationProvider
        ));
        log.trace("[Goya] |- security [authentication] |- bean [securityAuthenticationManager] register.");
        return providerManager;
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityAuthController securityAuthController(PasswordAuthService passwordAuthService,
                                                         SmsAuthService smsAuthService,
                                                         SocialAuthService socialAuthService,
                                                         WxMiniProgramAuthService wxMiniProgramAuthService,
                                                         MfaService mfaService,
                                                         @Qualifier("securityAuthenticationManager") AuthenticationManager securityAuthenticationManager) {
        SecurityAuthController securityAuthController = new SecurityAuthController(
                passwordAuthService,
                smsAuthService,
                socialAuthService,
                wxMiniProgramAuthService,
                mfaService,
                securityAuthenticationManager
        );
        log.trace("[Goya] |- security [authentication] |- bean [securityAuthController] register.");
        return securityAuthController;
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public SecurityLoginViewController securityLoginViewController() {
        SecurityLoginViewController securityLoginViewController = new SecurityLoginViewController();
        log.trace("[Goya] |- security [authentication] |- bean [securityLoginViewController] register.");
        return securityLoginViewController;
    }
}
