package com.ysmjjsy.goya.component.security.authorization.configuration.security;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.security.api.AuthorizationService;
import com.ysmjjsy.goya.component.security.authorization.configuration.properties.SecurityAuthorizationProperties;
import com.ysmjjsy.goya.component.security.authorization.filter.HeaderClaimConsistencyFilter;
import com.ysmjjsy.goya.component.security.authorization.filter.PolicyAuthorizationFilter;
import com.ysmjjsy.goya.component.security.authorization.filter.RevokedTokenFilter;
import com.ysmjjsy.goya.component.security.authorization.introspection.SecurityOpaqueTokenIntrospector;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>资源服务安全配置</p>
 *
 * @author goya
 * @since 2026/2/10
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ResourceServerSecurityConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- security [authorization] ResourceServerSecurityConfiguration init.");
    }

    @Bean
    @Order(3)
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity httpSecurity,
                                                                 SecurityAuthorizationProperties securityAuthorizationProperties,
                                                                 ObjectProvider<CacheService> cacheServiceProvider,
                                                                 ObjectProvider<AuthorizationService> authorizationServiceProvider,
                                                                 ObjectProvider<RequestMappingHandlerMapping> requestMappingHandlerMappingProvider,
                                                                 ObjectProvider<JwtDecoder> jwtDecoderProvider,
                                                                 ObjectProvider<OpaqueTokenIntrospector> opaqueTokenIntrospectorProvider) throws Exception {
        var authorize = httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(registry -> {
                    securityAuthorizationProperties.permitAllPatternList()
                            .forEach(pattern -> registry.requestMatchers(pattern).permitAll());
                    registry.anyRequest().authenticated();
                });

        JwtDecoder jwtDecoder = jwtDecoderProvider.getIfAvailable();
        OpaqueTokenIntrospector opaqueTokenIntrospector = opaqueTokenIntrospectorProvider.getIfAvailable();

        switch (securityAuthorizationProperties.mode()) {
            case JWT -> {
                if (jwtDecoder == null) {
                    jwtDecoder = createJwtDecoder(securityAuthorizationProperties);
                }
                JwtDecoder finalJwtDecoder = jwtDecoder;
                authorize.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                        .decoder(finalJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));
            }
            case OPAQUE -> {
                if (opaqueTokenIntrospector == null) {
                    opaqueTokenIntrospector = createOpaqueTokenIntrospector(securityAuthorizationProperties);
                }
                OpaqueTokenIntrospector finalOpaqueTokenIntrospector = opaqueTokenIntrospector;
                authorize.oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(opaque -> opaque
                        .introspector(finalOpaqueTokenIntrospector)));
            }
            case AUTO -> {
                if (jwtDecoder == null && hasJwtConfig(securityAuthorizationProperties)) {
                    jwtDecoder = createJwtDecoder(securityAuthorizationProperties);
                }
                if (opaqueTokenIntrospector == null && hasOpaqueConfig(securityAuthorizationProperties)) {
                    opaqueTokenIntrospector = createOpaqueTokenIntrospector(securityAuthorizationProperties);
                }
                JwtDecoder finalJwtDecoder = jwtDecoder;
                OpaqueTokenIntrospector finalOpaqueTokenIntrospector = opaqueTokenIntrospector;
                authorize.oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(
                        authenticationManagerResolver(finalJwtDecoder, finalOpaqueTokenIntrospector)
                ));
            }
        }

        httpSecurity.exceptionHandling(ex -> ex
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()));

        httpSecurity.addFilterAfter(new HeaderClaimConsistencyFilter(securityAuthorizationProperties), BearerTokenAuthenticationFilter.class);

        CacheService cacheService = cacheServiceProvider.getIfAvailable();
        if (cacheService != null) {
            httpSecurity.addFilterAfter(
                    new RevokedTokenFilter(cacheService, securityAuthorizationProperties),
                    HeaderClaimConsistencyFilter.class
            );
        }

        AuthorizationService authorizationService = authorizationServiceProvider.getIfAvailable();
        if (authorizationService != null && securityAuthorizationProperties.policyEnabled()) {
            httpSecurity.addFilterAfter(
                    new PolicyAuthorizationFilter(
                            authorizationService,
                            securityAuthorizationProperties,
                            requestMappingHandlerMappingProvider
                    ),
                    HeaderClaimConsistencyFilter.class
            );
        }

        return httpSecurity.build();
    }

    private JwtDecoder createJwtDecoder(SecurityAuthorizationProperties securityAuthorizationProperties) {
        if (StringUtils.isNotBlank(securityAuthorizationProperties.issuerUri())) {
            return JwtDecoders.fromIssuerLocation(securityAuthorizationProperties.issuerUri());
        }
        if (StringUtils.isNotBlank(securityAuthorizationProperties.jwkSetUri())) {
            return NimbusJwtDecoder.withJwkSetUri(securityAuthorizationProperties.jwkSetUri()).build();
        }
        throw new IllegalStateException("JWT 模式需要配置 issuer-uri 或 jwk-set-uri");
    }

    private OpaqueTokenIntrospector createOpaqueTokenIntrospector(SecurityAuthorizationProperties securityAuthorizationProperties) {
        if (StringUtils.isAnyBlank(
                securityAuthorizationProperties.introspectionUri(),
                securityAuthorizationProperties.introspectionClientId(),
                securityAuthorizationProperties.introspectionClientSecret()
        )) {
            throw new IllegalStateException("OPAQUE 模式需要配置 introspection-uri/client-id/client-secret");
        }

        SpringOpaqueTokenIntrospector introspector = new SpringOpaqueTokenIntrospector(
                securityAuthorizationProperties.introspectionUri(),
                securityAuthorizationProperties.introspectionClientId(),
                securityAuthorizationProperties.introspectionClientSecret()
        );
        return new SecurityOpaqueTokenIntrospector(introspector);
    }

    private boolean hasJwtConfig(SecurityAuthorizationProperties securityAuthorizationProperties) {
        return StringUtils.isNotBlank(securityAuthorizationProperties.issuerUri())
                || StringUtils.isNotBlank(securityAuthorizationProperties.jwkSetUri());
    }

    private boolean hasOpaqueConfig(SecurityAuthorizationProperties securityAuthorizationProperties) {
        return StringUtils.isNoneBlank(
                securityAuthorizationProperties.introspectionUri(),
                securityAuthorizationProperties.introspectionClientId(),
                securityAuthorizationProperties.introspectionClientSecret()
        );
    }

    private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(JwtDecoder jwtDecoder,
                                                                                            OpaqueTokenIntrospector opaqueTokenIntrospector) {
        AuthenticationManager jwtAuthenticationManager = null;
        if (jwtDecoder != null) {
            JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
            jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter());
            jwtAuthenticationManager = jwtAuthenticationProvider::authenticate;
        }

        AuthenticationManager opaqueAuthenticationManager = null;
        if (opaqueTokenIntrospector != null) {
            OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider = new OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector);
            opaqueAuthenticationManager = opaqueTokenAuthenticationProvider::authenticate;
        }

        AuthenticationManager unsupportedAuthenticationManager = authentication -> {
            throw new IllegalStateException("No authentication manager available for resource server");
        };

        DefaultBearerTokenResolver tokenResolver = new DefaultBearerTokenResolver();
        AuthenticationManager finalJwtAuthenticationManager = jwtAuthenticationManager;
        AuthenticationManager finalOpaqueAuthenticationManager = opaqueAuthenticationManager;
        return request -> {
            String token = tokenResolver.resolve(request);
            boolean jwtLike = token != null && token.chars().filter(ch -> ch == '.').count() == 2;

            if (jwtLike && finalJwtAuthenticationManager != null) {
                return finalJwtAuthenticationManager;
            }
            if (!jwtLike && finalOpaqueAuthenticationManager != null) {
                return finalOpaqueAuthenticationManager;
            }
            if (finalJwtAuthenticationManager != null) {
                return finalJwtAuthenticationManager;
            }
            if (finalOpaqueAuthenticationManager != null) {
                return finalOpaqueAuthenticationManager;
            }
            return unsupportedAuthenticationManager;
        };
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            Set<GrantedAuthority> authorities = new LinkedHashSet<>(scopeAuthoritiesConverter.convert(jwt));
            toSet(jwt.getClaim(StandardClaimNamesConst.AUTHORITIES))
                    .forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority)));
            toSet(jwt.getClaim(StandardClaimNamesConst.ROLES))
                    .stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .forEach(roleAuthority -> authorities.add(new SimpleGrantedAuthority(roleAuthority)));
            return authorities;
        });

        return jwtAuthenticationConverter;
    }

    private Set<String> toSet(Object claimValue) {
        if (claimValue == null) {
            return Set.of();
        }

        if (claimValue instanceof Collection<?> collection) {
            Set<String> values = new LinkedHashSet<>();
            for (Object item : collection) {
                if (item != null) {
                    values.add(String.valueOf(item));
                }
            }
            return values;
        }

        if (claimValue instanceof String text) {
            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }

        return Set.of(String.valueOf(claimValue));
    }
}
