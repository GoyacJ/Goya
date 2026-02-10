package com.ysmjjsy.goya.component.security.authentication.configuration;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p>认证端点安全链</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityAuthenticationWebSecurityConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authentication] SecurityAuthenticationWebSecurityConfiguration init.");
    }

    @Order(2)
    @Bean
    @SuppressWarnings("all")
    public SecurityFilterChain securityAuthenticationFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .securityMatcher("/api/security/auth/**", "/security/login", "/security/login/session")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(Customizer.withDefaults());
        return httpSecurity.build();
    }
}
