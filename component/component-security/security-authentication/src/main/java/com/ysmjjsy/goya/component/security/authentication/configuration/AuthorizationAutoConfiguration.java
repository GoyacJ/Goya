package com.ysmjjsy.goya.component.security.authentication.configuration;

import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.configurer.SecurityAuthenticationProviderConfigurer;
import com.ysmjjsy.goya.component.security.authentication.password.PasswordPolicyValidator;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.social.service.SmsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Bean
    public SecurityAuthenticationProviderConfigurer securityAuthenticationProviderConfigurer(SecurityUserManager securityUserManager,
                                                                                             PasswordPolicyValidator passwordPolicyValidator,
                                                                                             SmsService smsService) {
        SecurityAuthenticationProviderConfigurer configurer = new SecurityAuthenticationProviderConfigurer(
                securityUserManager,
                passwordPolicyValidator,
                smsService
        );
        log.trace("[Goya] |- security [authentication] configurer auto configure.");
        return configurer;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
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
