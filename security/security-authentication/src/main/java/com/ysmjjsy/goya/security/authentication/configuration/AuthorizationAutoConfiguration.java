package com.ysmjjsy.goya.security.authentication.configuration;

import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.component.captcha.api.ICaptchaService;
import com.ysmjjsy.goya.security.authentication.provider.password.PasswordGrantAuthenticationConverter;
import com.ysmjjsy.goya.security.authentication.provider.password.PasswordGrantAuthenticationProvider;
import com.ysmjjsy.goya.security.authentication.provider.sms.SmsGrantAuthenticationConverter;
import com.ysmjjsy.goya.security.authentication.provider.sms.SmsGrantAuthenticationProvider;
import com.ysmjjsy.goya.security.authentication.token.TokenService;
import com.ysmjjsy.goya.security.authentication.userinfo.OAuth2UserInfoMapper;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>OAuth2授权服务器自动配置</p>
 * <p>配置OAuth2 Authorization Server的核心功能：</p>
 * <ul>
 *   <li>注册自定义Grant Type（Password、SMS）</li>
 *   <li>配置Token Introspection和Revocation端点</li>
 *   <li>配置PKCE强制要求</li>
 *   <li>配置CORS支持</li>
 *   <li>配置异常处理</li>
 * </ul>
 *
 * <p>参考文档：</p>
 * <ul>
 *   <li><a href="https://github.com/spring-projects/spring-authorization-server/blob/main/docs/modules/ROOT/pages/configuration-model.adoc">Spring Authorization Server Configuration</a></li>
 *   <li><a href="https://github.com/spring-projects/spring-authorization-server/blob/main/docs/modules/ROOT/pages/guides/how-to-ext-grant-type.adoc">Extending Grant Types</a></li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/17 23:10
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class AuthorizationAutoConfiguration {

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final ISecurityUserService securityUserService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2UserInfoMapper oAuth2UserInfoMapper;
    private final TokenService tokenService;

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authentication] AuthorizationAutoConfiguration auto configure.");
    }

    /**
     * 配置OAuth2授权服务器安全过滤器链
     * <p>这是授权服务器的核心配置，包括：</p>
     * <ul>
     *   <li>注册自定义Grant Type的Converter和Provider</li>
     *   <li>配置Token Introspection和Revocation端点</li>
     *   <li>配置PKCE强制要求</li>
     *   <li>配置CORS支持</li>
     * </ul>
     *
     * @param http HttpSecurity配置
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // 应用OAuth2 Authorization Server的默认配置
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // 获取OAuth2AuthorizationServerConfigurer
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        // 获取强制PKCE的RegisteredClientRepository（如果启用）
        RegisteredClientRepository clientRepository = getRegisteredClientRepository();

        // 配置授权服务器
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, (authorizationServer) -> {
                    // 使用强制PKCE的RegisteredClientRepository
                    authorizationServer.registeredClientRepository(clientRepository);
                    // 1. 配置Token端点，注册自定义Grant Type
                    authorizationServer
                            .tokenEndpoint(tokenEndpoint -> {
                                // 注册自定义Grant Type的Converter
                                tokenEndpoint
                                        .accessTokenRequestConverters(converters -> {
                                            converters.add(new PasswordGrantAuthenticationConverter());
                                            converters.add(new SmsGrantAuthenticationConverter());
                                            log.debug("[Goya] |- security [authentication] Custom grant type converters registered.");
                                        })
                                        // 注册自定义Grant Type的Provider
                                        .authenticationProviders(providers -> {
                                            // 通过ApplicationContext获取Bean，避免循环依赖
                                            var applicationContext = http.getSharedObject(org.springframework.context.ApplicationContext.class);
                                            try {
                                                var passwordProvider = applicationContext.getBean(PasswordGrantAuthenticationProvider.class);
                                                providers.add(passwordProvider);
                                                log.debug("[Goya] |- security [authentication] Password grant provider registered.");
                                            } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                                                log.debug("[Goya] |- security [authentication] Password grant provider not available (ICaptchaService not found).");
                                            }
                                            try {
                                                var smsProvider = applicationContext.getBean(SmsGrantAuthenticationProvider.class);
                                                providers.add(smsProvider);
                                                log.debug("[Goya] |- security [authentication] SMS grant provider registered.");
                                            } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                                                log.debug("[Goya] |- security [authentication] SMS grant provider not available (ICacheService not found).");
                                            }
                                        });
                            })
                            // 2. 配置Token Introspection端点（RFC 7662）
                            .tokenIntrospectionEndpoint(tokenIntrospectionEndpoint -> {
                                // 使用默认配置，支持JWT和Opaque Token的内省
                                tokenIntrospectionEndpoint.introspectionResponseHandler((request, response, authentication) -> {
                                    // 可以在这里自定义响应处理
                                    log.trace("[Goya] |- security [authentication] Token introspection request processed.");
                                });
                                log.debug("[Goya] |- security [authentication] Token introspection endpoint configured.");
                            })
                            // 3. 配置Token Revocation端点（RFC 7009）
                            .tokenRevocationEndpoint(tokenRevocationEndpoint -> {
                                // 使用默认配置，支持Token撤销
                                tokenRevocationEndpoint.revocationResponseHandler((request, response, authentication) -> {
                                    // 可以在这里自定义响应处理
                                    log.trace("[Goya] |- security [authentication] Token revocation request processed.");
                                });
                                log.debug("[Goya] |- security [authentication] Token revocation endpoint configured.");
                            })
                            // 4. 配置Authorization端点，强制PKCE（OAuth2.1要求）
                            .authorizationEndpoint(authorizationEndpoint -> {
                                // PKCE强制要求通过PkceEnforcingRegisteredClientRepository在客户端查找时自动应用
                                // 这里只需要配置端点即可，PKCE验证由Spring Authorization Server自动完成
                                log.debug("[Goya] |- security [authentication] Authorization endpoint configured with PKCE support.");
                            })
                            // 5. 配置OIDC支持
                            .oidc(oidc -> {
                                oidc.userInfoEndpoint(userInfoEndpoint ->
                                        userInfoEndpoint.userInfoMapper(oAuth2UserInfoMapper));
                                log.debug("[Goya] |- security [authentication] OIDC user info endpoint configured.");
                            });
                })
                // 6. 配置CORS支持（前后端分离）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 7. 配置异常处理
                .exceptionHandling(exceptionHandling -> {
                    exceptionHandling
                            .authenticationEntryPoint((request, response, authException) -> {
                                log.warn("[Goya] |- security [authentication] Authentication failed: {}", authException.getMessage());
                                response.setStatus(401);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"error\":\"unauthorized\",\"error_description\":\"" + authException.getMessage() + "\"}");
                            })
                            .accessDeniedHandler((request, response, accessDeniedException) -> {
                                log.warn("[Goya] |- security [authentication] Access denied: {}", accessDeniedException.getMessage());
                                response.setStatus(403);
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write("{\"error\":\"access_denied\",\"error_description\":\"" + accessDeniedException.getMessage() + "\"}");
                            });
                });

        return http.build();
    }

    /**
     * 创建Password Grant Type认证提供者
     *
     * @param captchaService 验证码服务（可选）
     * @return PasswordGrantAuthenticationProvider
     */
    @Bean
    @ConditionalOnBean(ICaptchaService.class)
    public PasswordGrantAuthenticationProvider passwordGrantAuthenticationProvider(
            ICaptchaService captchaService) {
        return new PasswordGrantAuthenticationProvider(
                registeredClientRepository,
                authorizationService,
                securityUserService,
                passwordEncoder,
                tokenService,
                captchaService
        );
    }

    /**
     * 创建SMS Grant Type认证提供者
     *
     * @param cacheService 缓存服务（可选）
     * @return SmsGrantAuthenticationProvider
     */
    @Bean
    @ConditionalOnBean(ICacheService.class)
    public SmsGrantAuthenticationProvider smsGrantAuthenticationProvider(
            ICacheService cacheService) {
        return new SmsGrantAuthenticationProvider(
                registeredClientRepository,
                authorizationService,
                securityUserService,
                tokenService,
                cacheService
        );
    }

    /**
     * 配置CORS
     * <p>支持前后端分离架构，允许跨域请求</p>
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源（生产环境应该配置具体的域名）
        configuration.setAllowedOriginPatterns(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 获取RegisteredClientRepository
     * <p>如果启用了PKCE强制要求，返回包装后的Repository</p>
     *
     * @return RegisteredClientRepository
     */
    private RegisteredClientRepository getRegisteredClientRepository() {
        if (authenticationProperties.ssoConfig().requirePkce()) {
            log.debug("[Goya] |- security [authentication] PKCE enforcement enabled, wrapping RegisteredClientRepository.");
            return new com.ysmjjsy.goya.security.authentication.repository.PkceEnforcingRegisteredClientRepository(
                    registeredClientRepository,
                    authenticationProperties.ssoConfig());
        }
        return registeredClientRepository;
    }
}
