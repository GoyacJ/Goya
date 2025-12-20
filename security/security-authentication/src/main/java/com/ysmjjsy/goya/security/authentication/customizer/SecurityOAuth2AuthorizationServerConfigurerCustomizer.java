package com.ysmjjsy.goya.security.authentication.customizer;

import com.ysmjjsy.goya.security.authentication.userinfo.OAuth2UserInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.config.Customizer;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/17 23:13
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityOAuth2AuthorizationServerConfigurerCustomizer implements Customizer<OAuth2AuthorizationServerConfigurer> {

    private final OAuth2UserInfoMapper oAuth2UserInfoMapper;

    @Override
    public void customize(OAuth2AuthorizationServerConfigurer configurer) {
        configurer
                .oidc(oidc -> {
                    oidc.userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userInfoMapper(oAuth2UserInfoMapper));
                });
    }
}
