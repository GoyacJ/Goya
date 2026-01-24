package com.ysmjjsy.goya.component.security.oauth2.configuration;

import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.security.authentication.configurer.SecurityAuthenticationProviderConfigurer;
import com.ysmjjsy.goya.component.security.authentication.filter.CaptchaValidationFilter;
import com.ysmjjsy.goya.component.security.authentication.filter.DeviceManagementFilter;
import com.ysmjjsy.goya.component.security.authentication.handler.SecurityAuthenticationFailureHandler;
import com.ysmjjsy.goya.component.security.authentication.provider.login.PasswordAuthenticationConverter;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SmsAuthenticationConverter;
import com.ysmjjsy.goya.component.security.authentication.provider.login.SocialAuthenticationConverter;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.core.service.ITenantService;
import com.ysmjjsy.goya.component.security.core.tenant.PathTenantIdResolver;
import com.ysmjjsy.goya.component.security.core.tenant.TenantIdResolver;
import com.ysmjjsy.goya.component.security.oauth2.request.entrypoint.OAuth2AuthenticationEntryPoint;
import com.ysmjjsy.goya.component.security.oauth2.request.handler.OAuth2AuthenticationSuccessHandler;
import com.ysmjjsy.goya.component.security.oauth2.tenant.DefaultTenantIssuerResolver;
import com.ysmjjsy.goya.component.security.oauth2.tenant.TenantIssuerResolver;
import com.ysmjjsy.goya.component.security.oauth2.tenant.TenantRequestFilter;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.server.authorization.web.OAuth2AuthorizationEndpointFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

/**
 * <p>授权服务器安全配置</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@ConditionalOnBean({RegisteredClientRepository.class, OAuth2AuthorizationService.class, OAuth2AuthorizationConsentService.class})
public class SecurityAuthorizationServerAutoConfiguration {

    private final SecurityCoreProperties securityCoreProperties;
    private final ObjectProvider<ITenantService> tenantServiceProvider;

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [oauth2] SecurityAuthorizationServerAutoConfiguration auto configure.");
    }

    @Bean
    public TenantIdResolver tenantIdResolver() {
        TenantIdResolver pathResolver = new PathTenantIdResolver("/t");
        return request -> {
            ITenantService tenantService = tenantServiceProvider.getIfAvailable();
            if (tenantService != null) {
                String tenantId = tenantService.resolveTenantId(request);
                if (tenantId != null && !tenantId.isBlank()) {
                    return tenantId;
                }
            }
            return pathResolver.resolveTenantId(request);
        };
    }

    @Bean
    public TenantIssuerResolver tenantIssuerResolver() {
        return new DefaultTenantIssuerResolver(securityCoreProperties, tenantServiceProvider);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        String issuer = securityCoreProperties.authServiceUri();
        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder()
                .multipleIssuersAllowed(true);
        if (issuer != null && !issuer.isBlank()) {
            builder.issuer(issuer);
        }
        return builder.build();
    }

    @Bean
    public TenantRequestFilter tenantRequestFilter(TenantIdResolver tenantIdResolver) {
        return new TenantRequestFilter(tenantIdResolver);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationFilter loginAuthenticationFilter(
            AuthenticationManager authenticationManager,
            CryptoProcessor cryptoProcessor,
            SecurityAuthenticationFailureHandler failureHandler,
            OAuth2AuthenticationSuccessHandler successHandler) {
        DelegatingAuthenticationConverter converter = new DelegatingAuthenticationConverter(List.of(
                new PasswordAuthenticationConverter(cryptoProcessor),
                new SmsAuthenticationConverter(cryptoProcessor),
                new SocialAuthenticationConverter(cryptoProcessor)
        ));
        AuthenticationFilter filter = new AuthenticationFilter(authenticationManager, converter);
        filter.setRequestMatcher(request ->
                "POST".equalsIgnoreCase(request.getMethod())
                        && request.getRequestURI() != null
                        && request.getRequestURI().endsWith("/login"));
        filter.setFailureHandler(failureHandler);
        filter.setSuccessHandler(successHandler);
        return filter;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            OAuth2TokenGenerator<?> tokenGenerator,
            OAuth2AuthorizationService authorizationService,
            OAuth2AuthorizationConsentService authorizationConsentService,
            RegisteredClientRepository registeredClientRepository,
            AuthorizationServerSettings authorizationServerSettings,
            SecurityAuthenticationProviderConfigurer securityAuthenticationProviderConfigurer,
            OAuth2AuthenticationEntryPoint authenticationEntryPoint,
            TenantRequestFilter tenantRequestFilter,
            CaptchaValidationFilter captchaValidationFilter,
            DeviceManagementFilter deviceManagementFilter,
            AuthenticationFilter loginAuthenticationFilter) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher("/t/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/t/*/login", "/t/*/assets/**",
                                "/t/*/.well-known/**", "/t/*/oauth2/jwks").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint))
                .formLogin(Customizer.withDefaults())
                .with(securityAuthenticationProviderConfigurer, config -> {})
                .with(authorizationServerConfigurer, authorizationServer -> authorizationServer
                        .registeredClientRepository(registeredClientRepository)
                        .authorizationService(authorizationService)
                        .authorizationConsentService(authorizationConsentService)
                        .authorizationServerSettings(authorizationServerSettings)
                        .tokenGenerator(tokenGenerator)
                        .oidc(Customizer.withDefaults())
                );

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(customizer -> {}));

        http.addFilterBefore(tenantRequestFilter, OAuth2AuthorizationEndpointFilter.class);
        http.addFilterBefore(captchaValidationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(loginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(deviceManagementFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
