package com.ysmjjsy.goya.component.security.oauth2.configuration;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.ysmjjsy.goya.component.security.authentication.service.PreAuthCodeService;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.oauth2.configuration.key.JdbcOAuth2JwkManager;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import com.ysmjjsy.goya.component.security.oauth2.configuration.security.AuthorizationServerSecurityConfiguration;
import com.ysmjjsy.goya.component.security.oauth2.configuration.security.PkceEnforcingRegisteredClientRepository;
import com.ysmjjsy.goya.component.security.oauth2.grant.PreAuthCodeGrantAuthenticationConverter;
import com.ysmjjsy.goya.component.security.oauth2.grant.PreAuthCodeGrantAuthenticationProvider;
import com.ysmjjsy.goya.component.security.oauth2.service.SecurityOAuth2TokenFormatResolver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <p>OAuth2 自动配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecurityOAuth2Properties.class)
@ConditionalOnProperty(prefix = "goya.security.oauth2", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(AuthorizationServerSecurityConfiguration.class)
public class SecurityOAuth2AutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [oauth2] SecurityOAuth2AutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean
    public SecurityOAuth2TokenFormatResolver securityOAuth2TokenFormatResolver(SecurityOAuth2Properties securityOAuth2Properties) {
        SecurityOAuth2TokenFormatResolver resolver = new SecurityOAuth2TokenFormatResolver(securityOAuth2Properties);
        log.trace("[Goya] |- security [oauth2] |- bean [securityOAuth2TokenFormatResolver] register.");
        return resolver;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationGrantType preAuthCodeGrantType(SecurityOAuth2Properties securityOAuth2Properties) {
        AuthorizationGrantType authorizationGrantType = new AuthorizationGrantType(securityOAuth2Properties.preAuthCodeGrantType());
        log.trace("[Goya] |- security [oauth2] |- bean [preAuthCodeGrantType] register.");
        return authorizationGrantType;
    }

    @Bean
    @ConditionalOnMissingBean
    public PreAuthCodeGrantAuthenticationConverter preAuthCodeGrantAuthenticationConverter(AuthorizationGrantType preAuthCodeGrantType) {
        PreAuthCodeGrantAuthenticationConverter converter = new PreAuthCodeGrantAuthenticationConverter(preAuthCodeGrantType);
        log.trace("[Goya] |- security [oauth2] |- bean [preAuthCodeGrantAuthenticationConverter] register.");
        return converter;
    }

    @Bean
    @ConditionalOnMissingBean
    public PreAuthCodeGrantAuthenticationProvider preAuthCodeGrantAuthenticationProvider(AuthorizationGrantType preAuthCodeGrantType,
                                                                                          OAuth2AuthorizationService oAuth2AuthorizationService,
                                                                                          OAuth2TokenGenerator<?> oAuth2TokenGenerator,
                                                                                          PreAuthCodeService preAuthCodeService,
                                                                                          SecurityOAuth2TokenFormatResolver securityOAuth2TokenFormatResolver) {
        PreAuthCodeGrantAuthenticationProvider provider = new PreAuthCodeGrantAuthenticationProvider(
                preAuthCodeGrantType,
                oAuth2AuthorizationService,
                oAuth2TokenGenerator,
                preAuthCodeService,
                securityOAuth2TokenFormatResolver
        );
        log.trace("[Goya] |- security [oauth2] |- bean [preAuthCodeGrantAuthenticationProvider] register.");
        return provider;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthorizationServerSettings authorizationServerSettings(SecurityOAuth2Properties securityOAuth2Properties) {
        SecurityOAuth2Properties.Endpoints endpoints = securityOAuth2Properties.endpoints();

        AuthorizationServerSettings.Builder builder = AuthorizationServerSettings.builder();
        if (StringUtils.isNotBlank(securityOAuth2Properties.issuer())) {
            builder.issuer(securityOAuth2Properties.issuer());
        }

        builder.authorizationEndpoint(endpoints.authorizationEndpoint())
                .tokenEndpoint(endpoints.tokenEndpoint())
                .jwkSetEndpoint(endpoints.jwkSetEndpoint())
                .tokenRevocationEndpoint(endpoints.revocationEndpoint())
                .tokenIntrospectionEndpoint(endpoints.introspectionEndpoint())
                .oidcLogoutEndpoint(endpoints.oidcLogoutEndpoint());

        AuthorizationServerSettings authorizationServerSettings = builder.build();
        log.trace("[Goya] |- security [oauth2] |- bean [authorizationServerSettings] register.");
        return authorizationServerSettings;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JdbcTemplate.class)
    public JdbcOAuth2JwkManager jdbcOAuth2JwkManager(JdbcTemplate jdbcTemplate,
                                                     SecurityOAuth2Properties securityOAuth2Properties) {
        JdbcOAuth2JwkManager jwkManager = new JdbcOAuth2JwkManager(jdbcTemplate, securityOAuth2Properties);
        log.trace("[Goya] |- security [oauth2] |- bean [jdbcOAuth2JwkManager] register.");
        return jwkManager;
    }

    @Bean(name = "jwkSource")
    @ConditionalOnBean(JdbcOAuth2JwkManager.class)
    @ConditionalOnMissingBean(name = "jwkSource")
    public JWKSource<SecurityContext> persistentJwkSource(JdbcOAuth2JwkManager jdbcOAuth2JwkManager) {
        JWKSource<SecurityContext> jwkSource = (selector, context) -> {
            java.util.List<JWK> jwks = jdbcOAuth2JwkManager.loadJwks();
            return selector.select(new JWKSet(jwks));
        };
        log.trace("[Goya] |- security [oauth2] |- bean [persistentJwkSource] register.");
        return jwkSource;
    }

    @Bean(name = "jwkSource")
    @ConditionalOnMissingBean(name = "jwkSource")
    public JWKSource<SecurityContext> inMemoryJwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        ImmutableJWKSet<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);
        log.warn("[Goya] |- security [oauth2] |- JDBC 不可用，回退到内存密钥（重启后失效）");
        log.trace("[Goya] |- security [oauth2] |- bean [inMemoryJwkSource] register.");
        return jwkSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        JwtDecoder jwtDecoder = OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        log.trace("[Goya] |- security [oauth2] |- bean [jwtDecoder] register.");
        return jwtDecoder;
    }

    @Bean
    @ConditionalOnBean(DataSource.class)
    @ConditionalOnMissingBean
    public RegisteredClientRepository registeredClientRepository(DataSource dataSource,
                                                                 SecurityOAuth2Properties securityOAuth2Properties) {
        JdbcRegisteredClientRepository delegate = new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
        RegisteredClientRepository registeredClientRepository = new PkceEnforcingRegisteredClientRepository(
                delegate,
                securityOAuth2Properties.requirePkceForPublicClients()
        );
        log.trace("[Goya] |- security [oauth2] |- bean [registeredClientRepository] register with PKCE enforcement.");
        return registeredClientRepository;
    }

    @Bean
    @ConditionalOnBean({DataSource.class, RegisteredClientRepository.class})
    @ConditionalOnMissingBean
    public OAuth2AuthorizationService oAuth2AuthorizationService(DataSource dataSource,
                                                                 RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource), registeredClientRepository);
        log.trace("[Goya] |- security [oauth2] |- bean [oAuth2AuthorizationService] register.");
        return service;
    }

    @Bean
    @ConditionalOnBean({DataSource.class, RegisteredClientRepository.class})
    @ConditionalOnMissingBean
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(DataSource dataSource,
                                                                               RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationConsentService service = new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource), registeredClientRepository);
        log.trace("[Goya] |- security [oauth2] |- bean [oAuth2AuthorizationConsentService] register.");
        return service;
    }

    @Bean
    @ConditionalOnMissingBean(name = "securityAccessTokenCustomizer")
    public OAuth2TokenCustomizer<JwtEncodingContext> securityAccessTokenCustomizer() {
        OAuth2TokenCustomizer<JwtEncodingContext> customizer = context -> {
            if (!(context.getPrincipal().getPrincipal() instanceof SecurityUser securityUser)) {
                return;
            }

            context.getClaims().claim(StandardClaimNamesConst.TENANT_ID, securityUser.getTenantId());
            context.getClaims().claim(StandardClaimNamesConst.ROLES, securityUser.getRoles());
            context.getClaims().claim(
                    StandardClaimNamesConst.AUTHORITIES,
                    securityUser.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet())
            );
            context.getClaims().claim(StandardClaimNamesConst.OPEN_ID, securityUser.getOpenId());
            context.getClaims().claim(StandardClaimNamesConst.CLIENT_ID, context.getRegisteredClient().getClientId());

            Object details = context.getPrincipal().getDetails();
            if (details instanceof Map<?, ?> detailsMap) {
                Map<String, Object> convertedMap = new LinkedHashMap<>();
                detailsMap.forEach((k, v) -> convertedMap.put(String.valueOf(k), v));
                Object sid = convertedMap.get(StandardClaimNamesConst.SID);
                Object mfa = convertedMap.get(StandardClaimNamesConst.MFA);
                Object clientType = convertedMap.get(StandardClaimNamesConst.CLIENT_TYPE);
                Object dpopJkt = convertedMap.get(StandardClaimNamesConst.JKT);

                if (sid != null) {
                    context.getClaims().claim(StandardClaimNamesConst.SID, sid);
                }
                if (mfa != null) {
                    context.getClaims().claim(StandardClaimNamesConst.MFA, mfa);
                }
                if (clientType != null) {
                    context.getClaims().claim(StandardClaimNamesConst.CLIENT_TYPE, clientType);
                }
                if (dpopJkt instanceof String jkt && StringUtils.isNotBlank(jkt)) {
                    context.getClaims().claim(StandardClaimNamesConst.CNF, Map.of(StandardClaimNamesConst.JKT, jkt));
                }
            }
        };
        log.trace("[Goya] |- security [oauth2] |- bean [securityAccessTokenCustomizer] register.");
        return customizer;
    }

    private static RSAKey generateRsa() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }
}
