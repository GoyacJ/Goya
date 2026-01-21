package com.ysmjjsy.goya.component.security.authentication.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.ysmjjsy.goya.component.security.authentication.audit.SecurityAuthenticationAuditListener;
import com.ysmjjsy.goya.component.security.authentication.captcha.DynamicLoginCaptchaStrategy;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.service.impl.CacheOAuth2AuthorizationService;
import com.ysmjjsy.goya.component.security.authentication.token.JwtTokenCustomizer;
import com.ysmjjsy.goya.component.security.authentication.token.TokenBlacklistStamp;
import com.ysmjjsy.goya.component.security.authentication.token.TokenManager;
import com.ysmjjsy.goya.component.security.authentication.userinfo.OAuth2UserInfoMapper;
import com.ysmjjsy.goya.component.security.authentication.userinfo.SocialOAuth2UserService;
import com.ysmjjsy.goya.component.security.core.enums.CertificateEnum;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.core.utils.DPoPKeyUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
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
    public SecurityAuthenticationAuditListener securityAuthenticationAuditListener(SecurityUserManager securityUserManager){
        SecurityAuthenticationAuditListener listener = new SecurityAuthenticationAuditListener(securityUserManager);
        log.trace("[Goya] |- security [authentication] securityAuthenticationAuditListener auto configure.");
        return listener;
    }

    @Bean
    public OAuth2UserInfoMapper oAuth2UserInfoMapper(SecurityUserManager securityUserService) {
        OAuth2UserInfoMapper mapper = new OAuth2UserInfoMapper(securityUserService);
        log.trace("[Goya] |- security [authentication] oAuth2UserInfoMapper auto configure.");
        return mapper;
    }

    @Bean
    public SocialOAuth2UserService socialOAuth2UserService(OidcUserService oidcUserService,
                                                           SecurityUserManager iSecurityUserService){
        SocialOAuth2UserService socialOAuth2UserService = new SocialOAuth2UserService(oidcUserService,iSecurityUserService);
        log.trace("[Goya] |- security [authentication] socialOAuth2UserService auto configure.");
        return socialOAuth2UserService;
    }

    @Bean
    public DynamicLoginCaptchaStrategy dynamicLoginCaptchaStrategy(SecurityAuthenticationProperties securityAuthenticationProperties){
        DynamicLoginCaptchaStrategy strategy = new DynamicLoginCaptchaStrategy(securityAuthenticationProperties.captcha());
        log.trace("[Goya] |- security [authentication] SecurityAuthenticationAutoConfiguration |- bean [dynamicLoginCaptchaStrategy] register.");
        return strategy;
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
        return (jwkSelector, _) -> jwkSelector.select(jwkSet);
    }

    /**
     * 配置JWT Encoder
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * 配置JWT Token自定义器（支持DPoP）
     *
     * @param dPoPKeyFingerprintService DPoP公钥指纹服务
     * @return JwtTokenCustomizer
     */
    @Bean
    public JwtTokenCustomizer jwtTokenCustomizer(DPoPKeyUtils dPoPKeyFingerprintService) {
        return new JwtTokenCustomizer(dPoPKeyFingerprintService);
    }

    /**
     * 配置OAuth2 Token生成器
     * <p>
     * 组合JWT Generator（access_token）和Opaque Refresh Token Generator
     * </p>
     */
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(JwtEncoder jwtEncoder, JwtTokenCustomizer jwtTokenCustomizer) {
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(jwtTokenCustomizer);

        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();

        return new DelegatingOAuth2TokenGenerator(
                jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }

    /**
     * 配置缓存 OAuth2授权服务（优先使用）
     * <p>实现全无状态设计，支持水平扩展</p>
     *
     * @param cacheService 缓存服务
     * @return RedisOAuth2AuthorizationService
     */
    @Bean
    @ConditionalOnBean(ICacheService.class)
    public OAuth2AuthorizationService cacheOAuth2AuthorizationService(ICacheService cacheService) {
        CacheOAuth2AuthorizationService service = new CacheOAuth2AuthorizationService(cacheService);
        log.trace("[Goya] |- security [authentication] cacheOAuth2AuthorizationService auto configure.");
        return service;
    }

    @Bean
    public TokenBlacklistStamp tokenBlacklistStamp(SecurityAuthenticationProperties properties){
        TokenBlacklistStamp stamp = new TokenBlacklistStamp(properties.tokenBlackList());
        log.trace("[Goya] |- security [authentication] tokenBlacklistStamp auto configure.");
        return stamp;
    }

    /**
     * 配置Token服务
     * <p>封装OAuth2 Token生成的完整流程</p>
     * <p>支持混合Token模式（JWT Access Token + Opaque Refresh Token）和Refresh Token Rotation</p>
     *
     * @param tokenGenerator       Token生成器
     * @param authorizationService 授权服务
     * @param securityUserService  用户服务
     * @param securityAuditService 审计服务
     * @param tokenBlacklistStamp  token黑名单管理
     * @return TokenService
     */
    @Bean
    public TokenManager tokenService(
            OAuth2TokenGenerator<?> tokenGenerator,
            OAuth2AuthorizationService authorizationService,
            SecurityUserManager securityUserService,
            SecurityAuditService securityAuditService,
            TokenBlacklistStamp tokenBlacklistStamp) {
        TokenManager tokenService = new TokenManager(
                tokenGenerator, authorizationService, securityUserService, securityAuditService, tokenBlacklistStamp
        );
        log.trace("[Goya] |- security [authentication] TokenService auto configure.");
        return tokenService;
    }
}
