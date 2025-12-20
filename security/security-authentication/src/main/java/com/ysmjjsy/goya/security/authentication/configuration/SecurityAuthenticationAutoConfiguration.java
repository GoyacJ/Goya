package com.ysmjjsy.goya.security.authentication.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.ysmjjsy.goya.component.common.utils.ResourceResolverUtils;
import com.ysmjjsy.goya.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.security.authentication.token.JwtTokenCustomizer;
import com.ysmjjsy.goya.security.authentication.userinfo.OAuth2UserInfoMapper;
import com.ysmjjsy.goya.security.core.enums.CertificateEnum;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.token.*;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 14:57
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({SecurityAuthenticationProperties.class})
@EnableWebSecurity
public class SecurityAuthenticationAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authentication] SecurityAuthenticationAutoConfiguration auto configure.");
    }

    @Bean
    public OAuth2UserInfoMapper oAuth2UserInfoMapper(ISecurityUserService securityUserService) {
        OAuth2UserInfoMapper mapper = new OAuth2UserInfoMapper(securityUserService);
        log.trace("[Goya] |- security [authentication] oAuth2UserInfoMapper auto configure.");
        return mapper;
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(SecurityAuthenticationProperties authenticationProperties) throws NoSuchAlgorithmException {
        SecurityAuthenticationProperties.Jwk jwk = authenticationProperties.jwk();
        KeyPair keyPair = null;
        if (jwk.certificate() == CertificateEnum.CUSTOM) {
            try {
                Resource[] resource = ResourceResolverUtils.getResources(jwk.jksKeyStore());
                if (ArrayUtils.isNotEmpty(resource)) {
                    KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource[0], jwk.jksStorePassword().toCharArray());
                    keyPair = keyStoreKeyFactory.getKeyPair(jwk.jksKeyAlias(), jwk.jksKeyPassword().toCharArray());
                }
            } catch (IOException e) {
                log.error("[Goya] |- Read custom certificate under resource folder error!", e);
            }

        } else {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    /**
     * 配置JWT Encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * 配置OAuth2 Token生成器
     * <p>
     * 组合JWT Generator（access_token）和Opaque Refresh Token Generator
     * </p>
     */
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(new JwtTokenCustomizer());

        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }
}
