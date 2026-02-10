package com.ysmjjsy.goya.component.security.authentication.configuration;

import com.ysmjjsy.goya.component.security.authentication.audit.SecurityAuthenticationAuditListener;
import com.ysmjjsy.goya.component.security.authentication.captcha.DynamicLoginCaptchaStrategy;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.errortimes.LoginFailureCacheManger;
import com.ysmjjsy.goya.component.security.authentication.filter.CaptchaValidationFilter;
import com.ysmjjsy.goya.component.security.authentication.filter.DeviceManagementFilter;
import com.ysmjjsy.goya.component.security.authentication.handler.SecurityAuthenticationFailureHandler;
import com.ysmjjsy.goya.component.security.authentication.password.PasswordPolicyValidator;
import com.ysmjjsy.goya.component.captcha.api.CaptchaService;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 14:57
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({SecurityAuthenticationProperties.class})
@EnableWebSecurity
@ComponentScan(basePackages = {
        "com.ysmjjsy.goya.component.security.authentication.controller",
        "com.ysmjjsy.goya.component.security.authentication.lockout",
        "com.ysmjjsy.goya.component.security.authentication.passwordreset",
        "com.ysmjjsy.goya.component.security.authentication.passwordexpiration",
        "com.ysmjjsy.goya.component.security.authentication.session",
        "com.ysmjjsy.goya.component.security.authentication.registration",
        "com.ysmjjsy.goya.component.security.authentication.profile"
})
public class SecurityAuthenticationAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authentication] SecurityAuthenticationAutoConfiguration auto configure.");
    }

    @Bean
    public SecurityAuthenticationFailureHandler securityAuthenticationFailureHandler(){
        SecurityAuthenticationFailureHandler securityAuthenticationFailureHandler = new SecurityAuthenticationFailureHandler();
        log.trace("[Goya] |- security [authentication] securityAuthenticationFailureHandler auto configure.");
        return securityAuthenticationFailureHandler;
    }

    @Bean
    public PasswordPolicyValidator passwordPolicyValidator(SecurityUserManager securityUserManager,
                                                           PasswordEncoder passwordEncoder,
                                                           SecurityAuthenticationProperties properties) {
        PasswordPolicyValidator passwordPolicyValidator = new PasswordPolicyValidator(securityUserManager, passwordEncoder, properties);
        log.trace("[Goya] |- security [authentication] passwordPolicyValidator auto configure.");
        return passwordPolicyValidator;
    }

    @Bean
    public LoginFailureCacheManger loginFailureCacheManger(SecurityAuthenticationProperties securityAuthenticationProperties){
        LoginFailureCacheManger loginFailureCacheManger = new LoginFailureCacheManger(securityAuthenticationProperties);
        log.trace("[Goya] |- security [authentication] loginFailureCacheManger auto configure.");
        return loginFailureCacheManger;
    }

    @Bean
    public SecurityAuthenticationAuditListener securityAuthenticationAuditListener(SecurityUserManager securityUserManager, 
                                                                                   LoginFailureCacheManger loginFailureCacheManger,
                                                                                   com.ysmjjsy.goya.component.security.authentication.lockout.AccountLockoutService accountLockoutService) {
        SecurityAuthenticationAuditListener listener = new SecurityAuthenticationAuditListener(
                securityUserManager, 
                loginFailureCacheManger,
                accountLockoutService
        );
        log.trace("[Goya] |- security [authentication] securityAuthenticationAuditListener auto configure.");
        return listener;
    }

    @Bean
    public DynamicLoginCaptchaStrategy dynamicLoginCaptchaStrategy(SecurityAuthenticationProperties securityAuthenticationProperties) {
        DynamicLoginCaptchaStrategy strategy = new DynamicLoginCaptchaStrategy(securityAuthenticationProperties.captcha());
        log.trace("[Goya] |- security [authentication] SecurityAuthenticationAutoConfiguration |- bean [dynamicLoginCaptchaStrategy] register.");
        return strategy;
    }

    @Bean
    public CaptchaValidationFilter captchaValidationFilter(DynamicLoginCaptchaStrategy loginCaptchaStrategy,
                                                           CaptchaService captchaService) {
        CaptchaValidationFilter filter = new CaptchaValidationFilter(loginCaptchaStrategy, captchaService);
        log.trace("[Goya] |- security [authentication] CaptchaValidationFilter auto configure.");
        return filter;
    }

    @Bean
    public DeviceManagementFilter deviceManagementFilter(SecurityUserManager securityUserManager) {
        DeviceManagementFilter filter = new DeviceManagementFilter(securityUserManager);
        log.trace("[Goya] |- security [authentication] DeviceManagementFilter auto configure.");
        return filter;
    }
}
