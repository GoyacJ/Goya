package com.ysmjjsy.goya.component.security.oauth2.configuration;

import com.ysmjjsy.goya.component.cache.multilevel.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.security.authentication.configurer.SecurityAuthenticationProviderConfigurer;
import com.ysmjjsy.goya.component.security.authentication.filter.CaptchaValidationFilter;
import com.ysmjjsy.goya.component.security.authentication.filter.DeviceManagementFilter;
import com.ysmjjsy.goya.component.security.authentication.ratelimit.RateLimitFilter;
import com.ysmjjsy.goya.component.security.oauth2.client.MobileAuthStateStore;
import com.ysmjjsy.goya.component.security.oauth2.filter.AuthorizationEndpointDPoPFilter;
import com.ysmjjsy.goya.component.security.oauth2.filter.MobileAuthTokenFilter;
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
    private final ObjectProvider<com.ysmjjsy.goya.component.cache.multilevel.service.MultiLevelCacheService> cacheServiceProvider;
    private final ObjectProvider<org.springframework.security.oauth2.jwt.JwtEncoder> jwtEncoderProvider;

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
            AuthenticationFilter loginAuthenticationFilter,
            ObjectProvider<MobileAuthTokenFilter> mobileAuthTokenFilterProvider,
            ObjectProvider<org.springframework.security.oauth2.client.registration.ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            ObjectProvider<com.ysmjjsy.goya.component.security.oauth2.userinfo.SocialOAuth2UserService> socialOAuth2UserServiceProvider,
            ObjectProvider<com.ysmjjsy.goya.component.security.oauth2.request.handler.OAuth2AuthenticationSuccessHandler> oauth2AuthenticationSuccessHandlerProvider,
            ObjectProvider<RateLimitFilter> rateLimitFilterProvider,
            ObjectProvider<AuthorizationEndpointDPoPFilter> authorizationEndpointDPoPFilterProvider) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher("/t/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.ignoringRequestMatchers(authorizationServerConfigurer.getEndpointsMatcher()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/t/*/login", "/t/*/login/sms/**", "/t/*/login/wxapp",
                                "/login/oauth2/**", "/oauth2/authorization/**",
                                "/t/*/oauth2/logout", "/t/*/oauth2/introspect", "/t/*/oauth2/revoke",
                                "/t/*/password-reset/**", "/t/*/register/**",
                                "/t/*/assets/**", "/t/*/.well-known/**", "/t/*/oauth2/jwks").permitAll()
                        .requestMatchers("/t/*/oauth2/devices/admin/**").hasAuthority("admin:user:revoke")
                        .requestMatchers("/t/*/oauth2/devices/**").authenticated()
                        .requestMatchers("/t/*/oauth2/admin/**").hasAuthority("admin:user:revoke")
                        .requestMatchers("/t/*/account/lockout/unlock", "/t/*/account/lockout/batch-unlock").hasAuthority("admin:user:unlock")
                        .requestMatchers("/t/*/sessions/user/**").hasAnyAuthority("admin:user:view", "admin:user:revoke")
                        .requestMatchers("/t/*/profile/{userId}").hasAuthority("admin:user:view")
                        .requestMatchers("/t/*/oauth2/consents/user/**").hasAuthority("admin:user:view")
                        .requestMatchers("/t/*/account/lockout/**", "/t/*/password/expiration/**", "/t/*/sessions/**", "/t/*/profile/**", "/t/*/oauth2/consents/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint))
                .formLogin(Customizer.withDefaults())
                // SSO 支持：使用 Session 存储登录状态（需要配置 Spring Session + Redis）
                .sessionManagement(session -> session
                        .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED))
                .with(securityAuthenticationProviderConfigurer, config -> {})
                .with(authorizationServerConfigurer, authorizationServer -> authorizationServer
                        .registeredClientRepository(registeredClientRepository)
                        .authorizationService(authorizationService)
                        .authorizationConsentService(authorizationConsentService)
                        .authorizationServerSettings(authorizationServerSettings)
                        .tokenGenerator(tokenGenerator)
                        .oidc(Customizer.withDefaults())
                );
        
        // 配置 OAuth2 Login（社交登录回调处理）
        // 注意：只有在 ClientRegistrationRepository 存在时才配置 OAuth2 Login
        org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository = 
                clientRegistrationRepositoryProvider.getIfAvailable();
        if (clientRegistrationRepository != null) {
            http.oauth2Login(oauth2Login -> {
                oauth2Login
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .loginPage("/t/{tenant}/login")
                        .authorizationEndpoint(authorizationEndpoint -> {
                            // 授权端点路径：/oauth2/authorization/{registrationId}
                            // Spring Security OAuth2 Client 标准路径，不支持路径变量
                            // 租户信息通过 TenantRequestFilter 从路径中提取
                            authorizationEndpoint.baseUri("/oauth2/authorization");
                        })
                        .redirectionEndpoint(redirectionEndpoint -> {
                            // 回调端点路径：/login/oauth2/code/{registrationId}
                            // Spring Security OAuth2 Client 标准路径，不支持路径变量
                            // 租户信息通过 TenantRequestFilter 从路径中提取
                            redirectionEndpoint.baseUri("/login/oauth2/code/*");
                        });
                
                // 配置 OAuth2UserService（用于获取用户信息）
                com.ysmjjsy.goya.component.security.oauth2.userinfo.SocialOAuth2UserService socialOAuth2UserService = 
                        socialOAuth2UserServiceProvider.getIfAvailable();
                if (socialOAuth2UserService != null) {
                    oauth2Login.userInfoEndpoint(userInfoEndpoint -> {
                        userInfoEndpoint.oidcUserService(socialOAuth2UserService);
                    });
                    log.debug("[Goya] |- security [oauth2] SocialOAuth2UserService configured for OAuth2 Login");
                }
                
                // 配置认证成功处理器
                com.ysmjjsy.goya.component.security.oauth2.request.handler.OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler = 
                        oauth2AuthenticationSuccessHandlerProvider.getIfAvailable();
                if (oauth2AuthenticationSuccessHandler != null) {
                    oauth2Login.successHandler(oauth2AuthenticationSuccessHandler);
                    log.debug("[Goya] |- security [oauth2] OAuth2AuthenticationSuccessHandler configured for OAuth2 Login");
                }
            });
            log.debug("[Goya] |- security [oauth2] OAuth2 Login configured for social login");
        } else {
            log.debug("[Goya] |- security [oauth2] ClientRegistrationRepository not available, skipping OAuth2 Login configuration");
        }

        http.oauth2ResourceServer(oauth2 -> oauth2.jwt(customizer -> {}));

        // 添加限流过滤器（最早执行）
        RateLimitFilter rateLimitFilter = rateLimitFilterProvider.getIfAvailable();
        if (rateLimitFilter != null) {
            http.addFilterBefore(rateLimitFilter, OAuth2AuthorizationEndpointFilter.class);
        }

        // 添加授权端点 DPoP Proof 验证过滤器
        AuthorizationEndpointDPoPFilter dPoPFilter = authorizationEndpointDPoPFilterProvider.getIfAvailable();
        if (dPoPFilter != null) {
            http.addFilterBefore(dPoPFilter, OAuth2AuthorizationEndpointFilter.class);
            log.trace("[Goya] |- security [oauth2] AuthorizationEndpointDPoPFilter configured");
        }

        http.addFilterBefore(tenantRequestFilter, OAuth2AuthorizationEndpointFilter.class);
        http.addFilterBefore(captchaValidationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(loginAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(deviceManagementFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 添加移动端授权过滤器（在授权端点之前）
        MobileAuthTokenFilter mobileAuthTokenFilter = mobileAuthTokenFilterProvider.getIfAvailable();
        if (mobileAuthTokenFilter != null) {
            http.addFilterBefore(mobileAuthTokenFilter, OAuth2AuthorizationEndpointFilter.class);
            log.trace("[Goya] |- security [oauth2] MobileAuthTokenFilter configured");
        }

        return http.build();
    }
}
