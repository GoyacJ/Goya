package com.ysmjjsy.goya.component.security.oauth2.configuration.security;

import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import com.ysmjjsy.goya.component.security.oauth2.grant.PreAuthCodeGrantAuthenticationConverter;
import com.ysmjjsy.goya.component.security.oauth2.grant.PreAuthCodeGrantAuthenticationProvider;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * <p>授权服务器安全配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "goya.security.oauth2", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AuthorizationServerSecurityConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [oauth2] AuthorizationServerSecurityConfiguration init.");
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity httpSecurity,
                                                                      SecurityOAuth2Properties securityOAuth2Properties,
                                                                      PreAuthCodeGrantAuthenticationConverter preAuthCodeGrantAuthenticationConverter,
                                                                      PreAuthCodeGrantAuthenticationProvider preAuthCodeGrantAuthenticationProvider) throws Exception {
        httpSecurity.oauth2AuthorizationServer(Customizer.withDefaults());
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = httpSecurity.getConfigurer(OAuth2AuthorizationServerConfigurer.class);
        if (authorizationServerConfigurer == null) {
            throw new IllegalStateException("OAuth2AuthorizationServerConfigurer not found");
        }

        authorizationServerConfigurer
                .tokenEndpoint(tokenEndpoint -> tokenEndpoint
                        .accessTokenRequestConverter(preAuthCodeGrantAuthenticationConverter)
                        .authenticationProvider(preAuthCodeGrantAuthenticationProvider)
                );

        if (securityOAuth2Properties.oidcEnabled()) {
            authorizationServerConfigurer.oidc(Customizer.withDefaults());
        }

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        httpSecurity
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .exceptionHandling(exceptionHandling -> exceptionHandling.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/security/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                ))
                .oauth2ResourceServer(oAuth2ResourceServer -> oAuth2ResourceServer.jwt(Customizer.withDefaults()));

        return httpSecurity.build();
    }
}
