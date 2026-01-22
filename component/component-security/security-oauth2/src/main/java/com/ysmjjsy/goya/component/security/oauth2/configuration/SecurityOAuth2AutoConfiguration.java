package com.ysmjjsy.goya.component.security.oauth2.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.ysmjjsy.goya.component.cache.multilevel.service.MultiLevelCacheService;
import com.ysmjjsy.goya.component.framework.context.SpringContext;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.core.enums.CertificateEnum;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.oauth2.authorization.CacheOAuth2AuthorizationService;
import com.ysmjjsy.goya.component.security.oauth2.request.CustomizerRequestCache;
import com.ysmjjsy.goya.component.security.oauth2.request.entrypoint.OAuth2AuthenticationEntryPoint;
import com.ysmjjsy.goya.component.security.oauth2.request.handler.OAuth2AuthenticationSuccessHandler;
import com.ysmjjsy.goya.component.security.oauth2.token.JwtTokenCustomizer;
import com.ysmjjsy.goya.component.security.oauth2.token.TokenBlacklistStamp;
import com.ysmjjsy.goya.component.security.oauth2.token.TokenManager;
import com.ysmjjsy.goya.component.security.oauth2.userinfo.OAuth2UserInfoMapper;
import com.ysmjjsy.goya.component.security.oauth2.userinfo.SocialOAuth2UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.*;

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
 * @since 2026/1/22 23:03
 */
@Slf4j
@AutoConfiguration
public class SecurityOAuth2AutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [oauth2] SecurityOAuth2AutoConfiguration auto configure.");
    }

    /**
     * 创建Redis RequestCache
     * <p>用于保存和恢复 SavedRequest，支持无状态环境</p>
     *
     * @param cacheService 缓存服务（可选）
     * @return RedisRequestCache
     */
    @Bean
    public CustomizerRequestCache customizerRequestCache(MultiLevelCacheService cacheService) {
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
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(CustomizerRequestCache customizerRequestCache) {
        OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(customizerRequestCache);
        log.trace("[Goya] |- security [authentication] OAuth2AuthenticationSuccessHandler auto configure.");
        return handler;
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
    @ConditionalOnBean(MultiLevelCacheService.class)
    public OAuth2AuthorizationService cacheOAuth2AuthorizationService(MultiLevelCacheService cacheService) {
        CacheOAuth2AuthorizationService service = new CacheOAuth2AuthorizationService(cacheService);
        log.trace("[Goya] |- security [authentication] cacheOAuth2AuthorizationService auto configure.");
        return service;
    }

    @Bean
    public TokenBlacklistStamp tokenBlacklistStamp(SecurityAuthenticationProperties properties) {
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
     * @param tokenBlacklistStamp  token黑名单管理
     * @return TokenService
     */
    @Bean
    public TokenManager tokenService(
            OAuth2TokenGenerator<?> tokenGenerator,
            OAuth2AuthorizationService authorizationService,
            SecurityUserManager securityUserService,
            TokenBlacklistStamp tokenBlacklistStamp) {
        TokenManager tokenService = new TokenManager(
                tokenGenerator, authorizationService, securityUserService, tokenBlacklistStamp
        );
        log.trace("[Goya] |- security [authentication] TokenService auto configure.");
        return tokenService;
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(SecurityAuthenticationProperties authenticationProperties) throws NoSuchAlgorithmException {
        SecurityAuthenticationProperties.Jwk jwk = authenticationProperties.jwk();
        KeyPair keyPair = null;
        if (jwk.certificate() == CertificateEnum.CUSTOM) {
            Resource[] resource = SpringContext.getResources(jwk.jksKeyStore());
            if (ArrayUtils.isNotEmpty(resource)) {
                KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource[0], jwk.jksStorePassword().toCharArray());
                keyPair = keyStoreKeyFactory.getKeyPair(jwk.jksKeyAlias(), jwk.jksKeyPassword().toCharArray());
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
     * @return JwtTokenCustomizer
     */
    @Bean
    public JwtTokenCustomizer jwtTokenCustomizer() {
        return new JwtTokenCustomizer();
    }

    @Bean
    public OAuth2UserInfoMapper oAuth2UserInfoMapper(SecurityUserManager securityUserService) {
        OAuth2UserInfoMapper mapper = new OAuth2UserInfoMapper(securityUserService);
        log.trace("[Goya] |- security [authentication] oAuth2UserInfoMapper auto configure.");
        return mapper;
    }

    @Bean
    public SocialOAuth2UserService socialOAuth2UserService(OidcUserService oidcUserService,
                                                           SecurityUserManager iSecurityUserService) {
        SocialOAuth2UserService socialOAuth2UserService = new SocialOAuth2UserService(oidcUserService, iSecurityUserService);
        log.trace("[Goya] |- security [authentication] socialOAuth2UserService auto configure.");
        return socialOAuth2UserService;
    }
}
