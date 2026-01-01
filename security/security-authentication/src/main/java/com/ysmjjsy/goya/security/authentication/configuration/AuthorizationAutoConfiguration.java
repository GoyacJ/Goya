package com.ysmjjsy.goya.security.authentication.configuration;

import com.ysmjjsy.goya.security.authentication.userinfo.OAuth2UserInfoMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/17 23:10
 */
@Slf4j
@AutoConfiguration
public class AuthorizationAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authentication] AuthorizationAutoConfiguration auto configure.");
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
            HttpSecurity http,
            OAuth2UserInfoMapper oAuth2UserInfoMapper
    ) throws Exception {

        return http.build();
    }
}
