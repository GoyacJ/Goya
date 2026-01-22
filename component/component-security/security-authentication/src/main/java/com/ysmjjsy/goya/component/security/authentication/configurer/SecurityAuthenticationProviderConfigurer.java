package com.ysmjjsy.goya.component.security.authentication.configurer;

import com.ysmjjsy.goya.component.security.authentication.password.PasswordPolicyValidator;
import com.ysmjjsy.goya.component.security.authentication.provider.login.PasswordAuthenticationProvider;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SmsAuthenticationProvider;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SocialAuthenticationProvider;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.social.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

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
    private final SmsService smsService;

    @Override
    public void configure(HttpSecurity builder) {
        PasswordAuthenticationProvider passwordAuthenticationProvider = new PasswordAuthenticationProvider(securityUserManager, passwordPolicyValidator);
        builder.authenticationProvider(passwordAuthenticationProvider);

        SmsAuthenticationProvider smsAuthenticationProvider = new SmsAuthenticationProvider(securityUserManager, smsService);
        builder.authenticationProvider(smsAuthenticationProvider);

        SocialAuthenticationProvider socialAuthenticationProvider = new SocialAuthenticationProvider(securityUserManager);
        builder.authenticationProvider(socialAuthenticationProvider);
    }
}
