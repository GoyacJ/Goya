package com.ysmjjsy.goya.component.security.authentication.configurer;

import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.password.PasswordPolicyValidator;
import com.ysmjjsy.goya.component.security.authentication.provider.login.PasswordAuthenticationProvider;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SmsAuthenticationProvider;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SocialAuthenticationProvider;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import com.ysmjjsy.goya.component.security.core.service.IOtpService;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/22 23:25
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityAuthenticationProviderConfigurer extends AbstractHttpConfigurer<SecurityAuthenticationProviderConfigurer, HttpSecurity> {

    private final SecurityUserManager securityUserManager;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final ObjectProvider<IOtpService> otpServiceProvider;
    private final SecurityAuthenticationProperties authenticationProperties;

    @Override
    public void configure(HttpSecurity builder) {
        SecurityAuthenticationProperties.LoginConfig loginConfig = authenticationProperties.login();

        if (loginConfig.allowPasswordLogin()) {
            PasswordAuthenticationProvider passwordAuthenticationProvider =
                    new PasswordAuthenticationProvider(securityUserManager, passwordPolicyValidator);
            builder.authenticationProvider(passwordAuthenticationProvider);
        }

        if (loginConfig.allowSmsLogin()) {
            IOtpService otpService = otpServiceProvider.getIfAvailable();
            if (otpService != null) {
                SmsAuthenticationProvider smsAuthenticationProvider =
                        new SmsAuthenticationProvider(securityUserManager, otpService);
                builder.authenticationProvider(smsAuthenticationProvider);
            } else {
                log.warn("[Goya] |- security [authentication] SMS login enabled but IOtpService is missing.");
            }
        }

        if (loginConfig.allowSocialLogin()) {
            SocialAuthenticationProvider socialAuthenticationProvider =
                    new SocialAuthenticationProvider(securityUserManager);
            builder.authenticationProvider(socialAuthenticationProvider);
        }
    }
}
