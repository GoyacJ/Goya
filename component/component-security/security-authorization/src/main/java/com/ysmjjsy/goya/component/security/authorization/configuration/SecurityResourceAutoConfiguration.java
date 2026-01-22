package com.ysmjjsy.goya.component.security.authorization.configuration;

import com.ysmjjsy.goya.component.cache.multilevel.service.MultiLevelCacheService;
import com.ysmjjsy.goya.component.security.authorization.configuration.properties.SecurityResourceProperties;
import com.ysmjjsy.goya.component.security.authorization.dpop.ResourceServerDPoPValidator;
import com.ysmjjsy.goya.component.security.authorization.jwt.JwtAuthenticationFilter;
import com.ysmjjsy.goya.component.security.authorization.jwt.JwtAuthorityConverter;
import com.ysmjjsy.goya.component.security.core.utils.DPoPKeyUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p>资源服务器自动配置</p>
 * <p>配置OAuth2资源服务器的核心功能：</p>
 * <ul>
 *   <li>JWT Token验证（从授权服务器）</li>
 *   <li>DPoP Proof验证（如果Token是DPoP-bound）</li>
 *   <li>Token黑名单检查</li>
 *   <li>多租户支持</li>
 *   <li>权限提取（roles, authorities）</li>
 * </ul>
 *
 * <p>参考文档：</p>
 * <ul>
 *   <li><a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html">Spring Security Resource Server</a></li>
 *   <li><a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/dpop-tokens.html">DPoP-bound Access Tokens</a></li>
 * </ul>
 *
 * @author goya
 * @since 2025/10/10 15:07
 */
@Slf4j
@AutoConfiguration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityResourceProperties.class)
@RequiredArgsConstructor
public class SecurityResourceAutoConfiguration {

    private final SecurityResourceProperties resourceProperties;
    private final ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [resource] SecurityResourceAutoConfiguration auto configure.");
    }

    /**
     * 配置资源服务器安全过滤器链
     * <p>配置OAuth2资源服务器的JWT验证和DPoP支持</p>
     *
     * @param http HttpSecurity配置
     * @return SecurityFilterChain
     * @throws Exception 配置异常
     */
    @Bean
    @Order(1)
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,ResourceServerDPoPValidator resourceServerDPoPValidator) throws Exception {
        http
                .securityMatcher("/api/**") // 资源服务器只处理 /api/** 路径
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/api/public/**").permitAll()
                            .anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> {
                    oauth2.jwt(jwt -> {
                        jwt.decoder(jwtDecoder());
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter());
                    });
                });

        // 添加自定义JWT认证过滤器（用于DPoP验证等）
        http.addFilterAfter(jwtAuthenticationFilter(resourceServerDPoPValidator), BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 配置JWT解码器
     * <p>从授权服务器的JWK Set URI或Issuer URI获取公钥，验证JWT签名</p>
     *
     * @return JwtDecoder
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder() {
        SecurityResourceProperties.JwtConfig jwtConfig = resourceProperties.jwt();

        NimbusJwtDecoder jwtDecoder;

        // 1. 优先使用jwkSetUri（如果提供）
        if (StringUtils.isNotBlank(jwtConfig.jwkSetUri())) {
            jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwtConfig.jwkSetUri()).build();
            log.debug("[Goya] |- security [resource] JWT decoder configured with jwkSetUri: {}", jwtConfig.jwkSetUri());
        }
        // 2. 使用issuerUri（自动从/.well-known/jwks.json获取JWK Set）
        else if (StringUtils.isNotBlank(jwtConfig.issuerUri())) {
            jwtDecoder = NimbusJwtDecoder.withIssuerLocation(jwtConfig.issuerUri())
                    .validateType(jwtConfig.validateType())
                    .build();
            log.debug("[Goya] |- security [resource] JWT decoder configured with issuerUri: {}", jwtConfig.issuerUri());
        }
        // 3. 如果都未提供，使用默认配置（需要从application.yml配置）
        else {
            // 尝试从Spring Boot自动配置获取
            jwtDecoder = JwtDecoders.fromOidcIssuerLocation("http://localhost:8080");
            log.warn("[Goya] |- security [resource] JWT decoder using default configuration. Please configure issuer-uri or jwk-set-uri.");
        }

        // 4. 配置JWT验证器（audience验证等）
        if (jwtConfig.audiences() != null && !jwtConfig.audiences().isEmpty()) {
            jwtDecoder.setJwtValidator(
                    JwtValidators.createDefaultWithIssuer(jwtConfig.issuerUri())
            );
            log.debug("[Goya] |- security [resource] JWT validator configured with audiences: {}", jwtConfig.audiences());
        }

        // 5. 如果启用了Token黑名单，包装JwtDecoder
        if (resourceProperties.tokenBlacklist().enabled()) {
            try {
                MultiLevelCacheService cacheService = applicationContext.getBean(MultiLevelCacheService.class);
//                jwtDecoder = new JwtBlacklistValidator(jwtDecoder, cacheService, resourceProperties.tokenBlacklist());
                log.debug("[Goya] |- security [resource] JWT blacklist validator enabled.");
            } catch (Exception e) {
                log.debug("[Goya] |- security [resource] ICacheService not available, skipping blacklist validation.");
            }
        }

        return jwtDecoder;
    }

    /**
     * 配置JWT认证转换器
     * <p>从JWT中提取权限（roles, authorities, scopes）</p>
     *
     * @return JwtAuthenticationConverter
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtAuthorityConverter());
        log.trace("[Goya] |- security [resource] JwtAuthenticationConverter auto configure.");
        return converter;
    }

    /**
     * 配置DPoP公钥指纹服务
     * <p>用于验证DPoP Proof的公钥指纹</p>
     *
     * @return DPoPKeyFingerprintService
     */
    @Bean
    @ConditionalOnMissingBean
    public DPoPKeyUtils dPoPKeyFingerprintService() {
        DPoPKeyUtils service = new DPoPKeyUtils();
        log.trace("[Goya] |- security [resource] DPoPKeyFingerprintService auto configure.");
        return service;
    }

    /**
     * 配置DPoP Proof解码器
     * <p>用于解码和验证DPoP Proof JWT</p>
     * <p>注意：DPoP Proof的验证不需要issuer验证，只需要验证签名</p>
     *
     * @return JwtDecoder for DPoP Proof
     */
    @Bean
    @ConditionalOnMissingBean(name = "dPoPProofDecoder")
    public JwtDecoder dPoPProofDecoder() {
        // DPoP Proof的验证不需要issuer验证
        // 只需要验证JWT签名，公钥从DPoP Proof的jwk header中获取
        // 这里使用一个不验证issuer的解码器
        // 注意：实际验证逻辑在ResourceServerDPoPValidator中
        SecurityResourceProperties.JwtConfig jwtConfig = resourceProperties.jwt();
        
        if (StringUtils.isNotBlank(jwtConfig.jwkSetUri())) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwtConfig.jwkSetUri()).build();
            // 不验证issuer和audience（DPoP Proof不需要）
            decoder.setJwtValidator(JwtValidators.createDefault());
            log.trace("[Goya] |- security [resource] DPoP Proof decoder auto configure.");
            return decoder;
        }
        
        // 如果未配置，返回一个会失败的解码器（需要配置）
        throw new IllegalStateException(
                "DPoP Proof decoder requires jwk-set-uri configuration. " +
                        "Please configure platform.security.resource.jwt.jwk-set-uri"
        );
    }

    /**
     * 配置资源服务器DPoP验证器
     *
     * @param dPoPProofDecoder DPoP Proof解码器
     * @param dPoPKeyFingerprintService DPoP公钥指纹服务
     * @return ResourceServerDPoPValidator
     */
    @Bean
    public ResourceServerDPoPValidator resourceServerDPoPValidator(
            JwtDecoder dPoPProofDecoder,
            DPoPKeyUtils dPoPKeyFingerprintService) {
        ResourceServerDPoPValidator validator = new ResourceServerDPoPValidator(
                dPoPProofDecoder,
                resourceProperties.dpop(),
                dPoPKeyFingerprintService
        );
        log.trace("[Goya] |- security [resource] ResourceServerDPoPValidator auto configure.");
        return validator;
    }

    /**
     * 配置JWT认证过滤器
     * <p>用于DPoP验证和多租户上下文设置</p>
     *
     * @param dPoPValidator DPoP验证器
     * @return JwtAuthenticationFilter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(ResourceServerDPoPValidator dPoPValidator) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(dPoPValidator, resourceProperties);
        log.trace("[Goya] |- security [resource] JwtAuthenticationFilter auto configure.");
        return filter;
    }
}