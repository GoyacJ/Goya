package com.ysmjjsy.goya.security.authentication.configuration;

import com.ysmjjsy.goya.component.cache.crypto.CryptoProcessor;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.security.authentication.entrypoint.OAuth2AuthenticationEntryPoint;
import com.ysmjjsy.goya.security.authentication.filter.UnifiedLoginAuthenticationFilter;
import com.ysmjjsy.goya.security.authentication.handler.OAuth2AuthenticationSuccessHandler;
import com.ysmjjsy.goya.security.authentication.password.PasswordPolicyValidator;
import com.ysmjjsy.goya.security.authentication.provider.login.*;
import com.ysmjjsy.goya.security.authentication.request.CustomizerRequestCache;
import com.ysmjjsy.goya.security.core.manager.SecurityUserManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * <p>OAuth2授权服务器自动配置（OAuth 2.1）</p>
 * <p>配置OAuth2 Authorization Server的核心功能：</p>
 * <ul>
 *   <li>Authorization Code + PKCE流程（OAuth 2.1标准）</li>
 *   <li>统一登录认证（密码、短信、社交）</li>
 *   <li>配置Token Introspection和Revocation端点</li>
 *   <li>配置PKCE强制要求（Public Client）</li>
 *   <li>配置CORS支持</li>
 *   <li>配置无状态Session</li>
 *   <li>配置异常处理</li>
 * </ul>
 *
 * <p>参考文档：</p>
 * <ul>
 *   <li><a href="https://github.com/spring-projects/spring-authorization-server/blob/main/docs/modules/ROOT/pages/configuration-model.adoc">Spring Authorization Server Configuration</a></li>
 *   <li><a href="https://www.rfc-editor.org/rfc/rfc9207">RFC 9207 - OAuth 2.0 Authorization Server Issuer Identification</a></li>
 * </ul>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
public class AuthorizationAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authentication] AuthorizationAutoConfiguration auto configure.");
    }

    /**
     * 配置OAuth2授权服务器安全过滤器链
     * <p>这是授权服务器的核心配置，基于OAuth 2.1标准</p>
     *
     * @param http HttpSecurity配置
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            AuthenticationConfiguration authenticationConfiguration,
            ApplicationContext applicationContext,
            CompositeAuthenticationConverter compositeAuthenticationConverter,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            CustomizerRequestCache customizerRequestCache) throws Exception {
        // 应用OAuth2 Authorization Server的默认配置
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        // 获取OAuth2AuthorizationServerConfigurer
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        // 配置授权服务器
        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                // 配置无状态Session（前后端分离）
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 禁用CSRF（前后端分离，使用Token认证）
                .csrf(csrf -> csrf.disable())
                // 配置RequestCache（Redis版本，用于保存和恢复SavedRequest）
                .requestCache(requestCache -> requestCache.requestCache(customizerRequestCache))
                .with(authorizationServerConfigurer, (authorizationServer) -> {
                    // 1. 配置Authorization端点（Authorization Code流程）
                    authorizationServer
                            .authorizationEndpoint(authorizationEndpoint -> {
                                // PKCE验证由Spring Authorization Server根据RegisteredClient配置自动完成
                                // Public Client强制PKCE，Confidential Client可选
                                log.debug("[Goya] |- security [authentication] Authorization endpoint configured with PKCE support.");
                            })
                            // 2. 配置Token端点
                            .tokenEndpoint(tokenEndpoint -> {
                                // 支持Refresh Token Grant（Refresh Token Rotation）
                                log.debug("[Goya] |- security [authentication] Token endpoint configured.");
                            })
                            // 3. 配置Token Introspection端点（RFC 7662）
                            .tokenIntrospectionEndpoint(tokenIntrospectionEndpoint -> {
                                tokenIntrospectionEndpoint.introspectionResponseHandler((request, response, authentication) -> {
                                    log.trace("[Goya] |- security [authentication] Token introspection request processed.");
                                });
                                log.debug("[Goya] |- security [authentication] Token introspection endpoint configured.");
                            })
                            // 4. 配置Token Revocation端点（RFC 7009）
                            .tokenRevocationEndpoint(tokenRevocationEndpoint -> {
                                tokenRevocationEndpoint.revocationResponseHandler((request, response, authentication) -> {
                                    log.trace("[Goya] |- security [authentication] Token revocation request processed.");
                                });
                                log.debug("[Goya] |- security [authentication] Token revocation endpoint configured.");
                            })
                            // 5. 配置OIDC支持
                            .oidc(oidc -> {
                                oidc.userInfoEndpoint(userInfoEndpoint ->
                                        userInfoEndpoint.userInfoMapper(oAuth2UserInfoMapper));
                                log.debug("[Goya] |- security [authentication] OIDC user info endpoint configured.");
                            });
                })
                // 6. 配置登录页面和登录认证
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/login/**", "/oauth2/authorize").permitAll()
                            .anyRequest().authenticated();
                })
                // 8. 配置登录过滤器（使用自定义Filter支持多Provider）
                .addFilterBefore(
                        unifiedLoginAuthenticationFilter(
                                compositeAuthenticationConverter,
                                authenticationConfiguration.getAuthenticationManager(),
                                oAuth2AuthenticationSuccessHandler),
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .formLogin(formLogin -> {
                    // 禁用默认的formLogin Filter，使用自定义Filter
                    formLogin.disable();
                    log.debug("[Goya] |- security [authentication] Default formLogin disabled, using UnifiedLoginAuthenticationFilter.");
                })
                // 9. 配置CORS支持（前后端分离）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 10. 配置异常处理
                .exceptionHandling(exceptionHandling -> {
                    // 尝试使用自定义的 OAuth2AuthenticationEntryPoint（如果可用）
                    try {
                        OAuth2AuthenticationEntryPoint entryPoint = applicationContext.getBean(OAuth2AuthenticationEntryPoint.class);
                        exceptionHandling.authenticationEntryPoint(entryPoint);
                        log.debug("[Goya] |- security [authentication] OAuth2AuthenticationEntryPoint configured.");
                    } catch (org.springframework.beans.factory.NoSuchBeanDefinitionException e) {
                        // 降级到默认实现
                        exceptionHandling.authenticationEntryPoint((request, response, authException) -> {
                            log.warn("[Goya] |- security [authentication] Authentication failed: {}", authException.getMessage());
                            response.setStatus(401);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"unauthorized\",\"error_description\":\"" + authException.getMessage() + "\"}");
                        });
                        log.debug("[Goya] |- security [authentication] Using default AuthenticationEntryPoint.");
                    }
                    exceptionHandling.accessDeniedHandler((request, response, accessDeniedException) -> {
                        log.warn("[Goya] |- security [authentication] Access denied: {}", accessDeniedException.getMessage());
                        response.setStatus(403);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"error\":\"access_denied\",\"error_description\":\"" + accessDeniedException.getMessage() + "\"}");
                    });
                });

        return http.build();
    }

    /**
     * 创建密码登录认证转换器
     *
     * @return PasswordAuthenticationConverter
     */
    @Bean
    public PasswordAuthenticationConverter passwordAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        PasswordAuthenticationConverter converter = new PasswordAuthenticationConverter(cryptoProcessor);
        log.trace("[Goya] |- security [authentication] PasswordAuthenticationConverter auto configure.");
        return converter;
    }

    /**
     * 创建短信登录认证转换器
     *
     * @return SmsAuthenticationConverter
     */
    @Bean
    public SmsAuthenticationConverter smsAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        SmsAuthenticationConverter converter = new SmsAuthenticationConverter(cryptoProcessor);
        log.trace("[Goya] |- security [authentication] SmsAuthenticationConverter auto configure.");
        return converter;
    }

    /**
     * 创建社交登录认证转换器
     *
     * @return SocialAuthenticationConverter
     */
    @Bean
    public SocialAuthenticationConverter socialAuthenticationConverter(CryptoProcessor cryptoProcessor) {
        SocialAuthenticationConverter converter = new SocialAuthenticationConverter(cryptoProcessor);
        log.trace("[Goya] |- security [authentication] SocialAuthenticationConverter auto configure.");
        return converter;
    }

    /**
     * 创建组合认证转换器
     * <p>按顺序尝试多个Converter，返回第一个非null的结果</p>
     *
     * @return CompositeAuthenticationConverter
     */
    @Bean
    public CompositeAuthenticationConverter compositeAuthenticationConverter() {
        java.util.List<org.springframework.security.web.authentication.AuthenticationConverter> converters = new java.util.ArrayList<>();
        converters.add(passwordAuthenticationConverter());
        converters.add(smsAuthenticationConverter());
        converters.add(socialAuthenticationConverter());

        CompositeAuthenticationConverter converter = new CompositeAuthenticationConverter(converters);
        log.trace("[Goya] |- security [authentication] CompositeAuthenticationConverter auto configure.");
        return converter;
    }

    /**
     * 创建统一登录认证过滤器
     * <p>支持多种登录方式（密码、短信、社交）</p>
     *
     * @param authenticationManager 认证管理器
     * @return UnifiedLoginAuthenticationFilter
     */
    @Bean
    public UnifiedLoginAuthenticationFilter unifiedLoginAuthenticationFilter(
            CompositeAuthenticationConverter compositeAuthenticationConverter,
            AuthenticationManager authenticationManager,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) {
        UnifiedLoginAuthenticationFilter filter = new UnifiedLoginAuthenticationFilter(compositeAuthenticationConverter, authenticationManager);
        filter.setAuthenticationSuccessHandler(oAuth2AuthenticationSuccessHandler);
        filter.setAuthenticationFailureHandler(loginFailureHandler());
        filter.setAllowSessionCreation(false); // 无状态，不创建Session
        log.trace("[Goya] |- security [authentication] UnifiedLoginAuthenticationFilter auto configure.");
        return filter;
    }

    /**
     * 创建密码登录认证提供者
     * <p>处理用户名密码登录方式的认证</p>
     *
     * @return PasswordAuthenticationProvider
     */
    @Bean
    public PasswordAuthenticationProvider passwordAuthenticationProvider(SecurityUserManager iSecurityUserService, PasswordPolicyValidator passwordPolicyValidator) {
        PasswordAuthenticationProvider provider = new PasswordAuthenticationProvider(iSecurityUserService, passwordPolicyValidator);
        log.trace("[Goya] |- security [authentication] PasswordAuthenticationProvider auto configure.");
        return provider;
    }

    /**
     * 创建短信登录认证提供者
     * <p>处理短信验证码登录方式的认证</p>
     *
     * @param cacheService 缓存服务（可选）
     * @return SmsAuthenticationProvider
     */
    @Bean
    @ConditionalOnBean(ICacheService.class)
    public SmsAuthenticationProvider smsAuthenticationProvider(
            SecurityUserManager iSecurityUserService,
            ICacheService cacheService) {
        SmsAuthenticationProvider provider = new SmsAuthenticationProvider(iSecurityUserService, cacheService);
        log.trace("[Goya] |- security [authentication] SmsAuthenticationProvider auto configure.");
        return provider;
    }

    /**
     * 创建社交登录认证提供者
     * <p>处理第三方社交登录方式的认证</p>
     *
     * @return SocialAuthenticationProvider
     */
    @Bean
    public SocialAuthenticationProvider socialAuthenticationProvider(SecurityUserManager iSecurityUserService, SecurityAuthenticationProperties securityAuthenticationProperties) {
        SocialAuthenticationProvider provider = new SocialAuthenticationProvider(iSecurityUserService, securityAuthenticationProperties);
        log.trace("[Goya] |- security [authentication] SocialAuthenticationProvider auto configure.");
        return provider;
    }

    /**
     * 创建Redis RequestCache
     * <p>用于保存和恢复 SavedRequest，支持无状态环境</p>
     *
     * @param cacheService 缓存服务（可选）
     * @return RedisRequestCache
     */
    @Bean
    @ConditionalOnBean(ICacheService.class)
    public CustomizerRequestCache customizerRequestCache(ICacheService cacheService) {
        CustomizerRequestCache requestCache = new CustomizerRequestCache(cacheService);
        log.trace("[Goya] |- security [authentication] customizerRequestCache auto configure.");
        return requestCache;
    }

    /**
     * 创建OAuth2认证入口点
     * <p>拦截未认证的请求，保存原始请求到Redis，重定向到登录页面</p>
     *
     * @return OAuth2AuthenticationEntryPoint
     */
    @Bean
    @ConditionalOnBean(ICacheService.class)
    public OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint(CustomizerRequestCache customizerRequestCache) {
        OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint(customizerRequestCache);
        log.trace("[Goya] |- security [authentication] OAuth2AuthenticationEntryPoint auto configure.");
        return entryPoint;
    }

    /**
     * 创建OAuth2认证成功处理器
     * <p>登录成功后，从Redis恢复SavedRequest，重定向回授权端点</p>
     *
     * @return OAuth2AuthenticationSuccessHandler
     */
    @Bean
    @ConditionalOnBean(ICacheService.class)
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(CustomizerRequestCache customizerRequestCache) {
        OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(customizerRequestCache);
        log.trace("[Goya] |- security [authentication] OAuth2AuthenticationSuccessHandler auto configure.");
        return handler;
    }

    /**
     * 创建登录失败处理器
     *
     * @return AuthenticationFailureHandler
     */
    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            log.warn("[Goya] |- security [authentication] Login failed: {}", exception.getMessage());
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"authentication_failed\",\"error_description\":\"" + exception.getMessage() + "\"}");
        };
    }

    /**
     * 配置CORS
     * <p>支持前后端分离架构，允许跨域请求</p>
     * <p>从配置文件中读取允许的域名，生产环境不允许使用*</p>
     *
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityAuthenticationProperties securityAuthenticationProperties) {
        CorsConfiguration configuration = new CorsConfiguration();

        SecurityAuthenticationProperties.CorsConfig corsConfig = securityAuthenticationProperties.cors();

        // 设置允许的来源（从配置读取）
        if (corsConfig.allowedOrigins() != null && !corsConfig.allowedOrigins().isEmpty()) {
            // 检查是否包含*（生产环境警告）
            if (corsConfig.allowedOrigins().contains("*")) {
                log.warn("[Goya] |- security [authentication] CORS配置包含'*'，生产环境不建议使用！请配置具体的域名。");
            }
            configuration.setAllowedOriginPatterns(corsConfig.allowedOrigins());
        } else {
            // 默认值：开发环境允许localhost
            log.warn("[Goya] |- security [authentication] CORS配置为空，使用默认值（仅允许localhost）。");
            configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "https://localhost:*"));
        }

        // 设置允许的HTTP方法
        if (corsConfig.allowedMethods() != null && !corsConfig.allowedMethods().isEmpty()) {
            configuration.setAllowedMethods(corsConfig.allowedMethods());
        } else {
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        }

        // 设置允许的请求头
        if (corsConfig.allowedHeaders() != null && !corsConfig.allowedHeaders().isEmpty()) {
            configuration.setAllowedHeaders(corsConfig.allowedHeaders());
        } else {
            configuration.setAllowedHeaders(Collections.singletonList("*"));
        }

        // 设置是否允许携带凭证
        configuration.setAllowCredentials(corsConfig.allowCredentials() != null ? corsConfig.allowCredentials() : true);

        // 设置预检请求缓存时间
        configuration.setMaxAge(corsConfig.maxAge() != null ? corsConfig.maxAge() : 3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.debug("[Goya] |- security [authentication] CORS配置完成，允许的来源: {}", configuration.getAllowedOriginPatterns());
        return source;
    }
}
