package com.ysmjjsy.goya.component.security.oauth2.configuration;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.ysmjjsy.goya.component.cache.multilevel.service.MultiLevelCacheService;
import com.ysmjjsy.goya.component.framework.context.SpringContext;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.core.configuration.properties.SecurityCoreProperties;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import com.ysmjjsy.goya.component.security.core.enums.CertificateEnum;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import com.ysmjjsy.goya.component.security.oauth2.request.CustomizerRequestCache;
import com.ysmjjsy.goya.component.security.oauth2.request.entrypoint.OAuth2AuthenticationEntryPoint;
import com.ysmjjsy.goya.component.security.oauth2.request.handler.OAuth2AuthenticationSuccessHandler;
import com.ysmjjsy.goya.component.security.oauth2.service.IOAuth2AuthorizationConsentService;
import com.ysmjjsy.goya.component.security.oauth2.service.IOAuth2AuthorizationService;
import com.ysmjjsy.goya.component.security.oauth2.service.IRegisteredClientService;
import com.ysmjjsy.goya.component.security.oauth2.service.adapter.OAuth2AuthorizationConsentServiceAdapter;
import com.ysmjjsy.goya.component.security.oauth2.service.adapter.OAuth2AuthorizationServiceAdapter;
import com.ysmjjsy.goya.component.security.oauth2.service.adapter.RegisteredClientRepositoryAdapter;
import com.ysmjjsy.goya.component.security.oauth2.token.JwtTokenCustomizer;
import com.ysmjjsy.goya.component.security.oauth2.token.TokenBlacklistStamp;
import com.ysmjjsy.goya.component.security.oauth2.token.TokenManager;
import com.ysmjjsy.goya.component.security.oauth2.userinfo.OAuth2UserInfoMapper;
import com.ysmjjsy.goya.component.security.oauth2.userinfo.SocialOAuth2UserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.encrypt.KeyStoreKeyFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

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
@EnableConfigurationProperties(SecurityOAuth2Properties.class)
@ComponentScan(basePackages = {
        "com.ysmjjsy.goya.component.security.oauth2.controller",
        "com.ysmjjsy.goya.component.security.oauth2.consent.controller"
})
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
    @ConditionalOnBean(MultiLevelCacheService.class)
    public RequestCache requestCache(MultiLevelCacheService cacheService) {
        CustomizerRequestCache requestCache =
                new CustomizerRequestCache(cacheService);
        log.trace("[Goya] |- security [authentication] customizerRequestCache auto configure.");
        return requestCache;
    }

    @Bean
    @ConditionalOnMissingBean(RequestCache.class)
    public RequestCache sessionRequestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        log.trace("[Goya] |- security [authentication] HttpSessionRequestCache auto configure.");
        return requestCache;
    }

    /**
     * 创建OAuth2认证入口点
     * <p>拦截未认证的请求，保存原始请求到Redis，重定向到登录页面</p>
     *
     * @return OAuth2AuthenticationEntryPoint
     */
    @Bean
    public OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint(RequestCache requestCache) {
        OAuth2AuthenticationEntryPoint entryPoint = new OAuth2AuthenticationEntryPoint(requestCache);
        log.trace("[Goya] |- security [authentication] OAuth2AuthenticationEntryPoint auto configure.");
        return entryPoint;
    }

    /**
     * 创建OAuth2认证成功处理器
     * <p>登录成功后，从Redis恢复SavedRequest，重定向回授权端点</p>
     * <p>支持多客户端类型：Web（Session）、移动端（临时Token）、小程序（直接Token）</p>
     *
     * @param requestCache           请求缓存
     * @param oauth2Properties       OAuth2 配置属性
     * @param clientTypeResolver     客户端类型识别器
     * @param tempAuthTokenGenerator 临时认证 Token 生成器
     * @param mobileAuthStateStore   移动端授权状态存储
     * @param registeredClientRepository 注册客户端仓库
     * @return OAuth2AuthenticationSuccessHandler
     */
    @Bean
    public OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(
            RequestCache requestCache,
            SecurityOAuth2Properties oauth2Properties,
            com.ysmjjsy.goya.component.security.oauth2.client.ClientTypeResolver clientTypeResolver,
            com.ysmjjsy.goya.component.security.oauth2.token.TemporaryAuthTokenGenerator tempAuthTokenGenerator,
            com.ysmjjsy.goya.component.security.oauth2.client.MobileAuthStateStore mobileAuthStateStore,
            RegisteredClientRepository registeredClientRepository,
            org.springframework.beans.factory.ObjectProvider<com.ysmjjsy.goya.component.security.authentication.mfa.service.MfaService> mfaServiceProvider,
            SecurityAuthenticationProperties authenticationProperties) {
        OAuth2AuthenticationSuccessHandler handler = new OAuth2AuthenticationSuccessHandler(
                requestCache,
                oauth2Properties,
                clientTypeResolver,
                tempAuthTokenGenerator,
                mobileAuthStateStore,
                registeredClientRepository,
                mfaServiceProvider,
                authenticationProperties);
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
     * @param authorizationService authorizationService
     * @return RedisOAuth2AuthorizationService
     */
    @Bean
    @ConditionalOnBean(IOAuth2AuthorizationService.class)
    public OAuth2AuthorizationService oAuth2AuthorizationService(IOAuth2AuthorizationService authorizationService) {
        OAuth2AuthorizationServiceAdapter adapter = new OAuth2AuthorizationServiceAdapter(authorizationService);
        log.trace("[Goya] |- security [oauth2] OAuth2AuthorizationService adapter auto configure.");
        return adapter;
    }

    @Bean
    @ConditionalOnBean(IOAuth2AuthorizationConsentService.class)
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(IOAuth2AuthorizationConsentService consentService) {
        OAuth2AuthorizationConsentServiceAdapter adapter = new OAuth2AuthorizationConsentServiceAdapter(consentService);
        log.trace("[Goya] |- security [oauth2] OAuth2AuthorizationConsentService adapter auto configure.");
        return adapter;
    }

    @Bean
    @ConditionalOnBean(IRegisteredClientService.class)
    public RegisteredClientRepository registeredClientRepository(IRegisteredClientService registeredClientService) {
        RegisteredClientRepositoryAdapter adapter = new RegisteredClientRepositoryAdapter(registeredClientService);
        log.trace("[Goya] |- security [oauth2] RegisteredClientRepository adapter auto configure.");
        return adapter;
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
    @ConditionalOnMissingBean(JWKSource.class)
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
    @ConditionalOnMissingBean(JwtEncoder.class)
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * 配置JWT Token自定义器（支持DPoP）
     *
     * @param passwordExpirationServiceProvider 密码过期服务提供者
     * @return JwtTokenCustomizer
     */
    @Bean
    public JwtTokenCustomizer jwtTokenCustomizer(
            org.springframework.beans.factory.ObjectProvider<com.ysmjjsy.goya.component.security.authentication.passwordexpiration.PasswordExpirationService> passwordExpirationServiceProvider) {
        return new JwtTokenCustomizer(passwordExpirationServiceProvider);
    }

    @Bean
    public OAuth2UserInfoMapper oAuth2UserInfoMapper(SecurityUserManager securityUserService) {
        OAuth2UserInfoMapper mapper = new OAuth2UserInfoMapper(securityUserService);
        log.trace("[Goya] |- security [authentication] oAuth2UserInfoMapper auto configure.");
        return mapper;
    }

    /**
     * 配置社交登录 OAuth2 用户服务
     * <p>处理第三方登录回调，支持用户自动创建和绑定</p>
     * <p>仅在启用社交登录时配置</p>
     *
     * @param oidcUserService OIDC 用户服务（Spring Security 默认实现）
     * @param iSecurityUserService 用户管理服务
     * @return SocialOAuth2UserService
     */
    @Bean
    @ConditionalOnProperty(prefix = "goya.security.authentication.login", name = "allow-social-login", havingValue = "true", matchIfMissing = false)
    public SocialOAuth2UserService socialOAuth2UserService(OidcUserService oidcUserService,
                                                           SecurityUserManager iSecurityUserService) {
        SocialOAuth2UserService socialOAuth2UserService = new SocialOAuth2UserService(oidcUserService, iSecurityUserService);
        log.trace("[Goya] |- security [oauth2] SocialOAuth2UserService auto configure.");
        return socialOAuth2UserService;
    }


    /**
     * 配置 Token 撤销服务
     *
     * @param authorizationService OAuth2 授权服务
     * @param tokenBlacklistStamp   Token 黑名单
     * @param securityUserManager   用户管理器
     * @param securityCoreProperties 安全核心配置
     * @return TokenRevocationService
     */
    @Bean
    @ConditionalOnBean({OAuth2AuthorizationService.class, TokenBlacklistStamp.class})
    public com.ysmjjsy.goya.component.security.oauth2.token.TokenRevocationService tokenRevocationService(
            OAuth2AuthorizationService authorizationService,
            TokenBlacklistStamp tokenBlacklistStamp,
            SecurityUserManager securityUserManager,
            SecurityCoreProperties securityCoreProperties) {
        // 创建 JwtDecoder（用于解析 JWT 获取 JTI）
        // 使用授权服务器的 JWK Set URI 创建 JwtDecoder
        org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder = null;
        String authServiceUri = securityCoreProperties.authServiceUri();
        if (authServiceUri != null && !authServiceUri.isBlank()) {
            try {
                // 构建 JWK Set URI（标准路径：/.well-known/jwks.json）
                String jwkSetUri = authServiceUri + "/.well-known/jwks.json";
                jwtDecoder = org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
                log.debug("[Goya] |- security [oauth2] TokenRevocationService JwtDecoder configured with jwkSetUri: {}", jwkSetUri);
            } catch (Exception e) {
                log.warn("[Goya] |- security [oauth2] Failed to create JwtDecoder for TokenRevocationService", e);
            }
        } else {
            log.warn("[Goya] |- security [oauth2] authServiceUri not configured, TokenRevocationService JwtDecoder will be null");
        }
        
        com.ysmjjsy.goya.component.security.oauth2.token.TokenRevocationService service =
                new com.ysmjjsy.goya.component.security.oauth2.token.TokenRevocationService(
                        authorizationService, tokenBlacklistStamp, securityUserManager, jwtDecoder);
        log.trace("[Goya] |- security [oauth2] TokenRevocationService auto configure.");
        return service;
    }

    /**
     * 配置 Token 内省服务
     *
     * @param authorizationService OAuth2 授权服务
     * @param tokenBlacklistStamp  Token 黑名单
     * @param securityCoreProperties 安全核心配置
     * @return TokenIntrospectionService
     */
    @Bean
    @ConditionalOnBean({OAuth2AuthorizationService.class, TokenBlacklistStamp.class})
    public com.ysmjjsy.goya.component.security.oauth2.token.TokenIntrospectionService tokenIntrospectionService(
            OAuth2AuthorizationService authorizationService,
            TokenBlacklistStamp tokenBlacklistStamp,
            SecurityCoreProperties securityCoreProperties) {
        // 创建 JwtDecoder（用于解析 JWT Access Token）
        org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder = null;
        String authServiceUri = securityCoreProperties.authServiceUri();
        if (authServiceUri != null && !authServiceUri.isBlank()) {
            try {
                String jwkSetUri = authServiceUri + "/.well-known/jwks.json";
                jwtDecoder = org.springframework.security.oauth2.jwt.NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
                log.debug("[Goya] |- security [oauth2] TokenIntrospectionService JwtDecoder configured with jwkSetUri: {}", jwkSetUri);
            } catch (Exception e) {
                log.warn("[Goya] |- security [oauth2] Failed to create JwtDecoder for TokenIntrospectionService", e);
            }
        } else {
            log.warn("[Goya] |- security [oauth2] authServiceUri not configured, TokenIntrospectionService JwtDecoder will be null");
        }
        
        com.ysmjjsy.goya.component.security.oauth2.token.TokenIntrospectionService service =
                new com.ysmjjsy.goya.component.security.oauth2.token.TokenIntrospectionService(
                        authorizationService, tokenBlacklistStamp, jwtDecoder);
        log.trace("[Goya] |- security [oauth2] TokenIntrospectionService auto configure.");
        return service;
    }
}
