package com.ysmjjsy.goya.component.security.oauth2.grant;

import com.ysmjjsy.goya.component.security.authentication.service.PreAuthCodeService;
import com.ysmjjsy.goya.component.security.authentication.service.model.PreAuthCodePayload;
import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import com.ysmjjsy.goya.component.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.oauth2.service.SecurityOAuth2TokenFormatResolver;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.security.Principal;
import java.util.*;

/**
 * <p>预认证码授权 Provider</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PreAuthCodeGrantAuthenticationProvider implements AuthenticationProvider {

    private final AuthorizationGrantType preAuthCodeGrantType;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final PreAuthCodeService preAuthCodeService;
    private final SecurityOAuth2TokenFormatResolver tokenFormatResolver;

    public PreAuthCodeGrantAuthenticationProvider(AuthorizationGrantType preAuthCodeGrantType,
                                                  OAuth2AuthorizationService authorizationService,
                                                  OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                  PreAuthCodeService preAuthCodeService,
                                                  SecurityOAuth2TokenFormatResolver tokenFormatResolver) {
        this.preAuthCodeGrantType = preAuthCodeGrantType;
        this.authorizationService = authorizationService;
        this.tokenGenerator = tokenGenerator;
        this.preAuthCodeService = preAuthCodeService;
        this.tokenFormatResolver = tokenFormatResolver;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthCodeGrantAuthenticationToken preAuthCodeGrantAuthentication = (PreAuthCodeGrantAuthenticationToken) authentication;

        OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(preAuthCodeGrantAuthentication);
        RegisteredClient registeredClient = tokenFormatResolver.resolve(
                clientPrincipal.getRegisteredClient(),
                resolveClientType(preAuthCodeGrantAuthentication)
        );

        if (!registeredClient.getAuthorizationGrantTypes().contains(preAuthCodeGrantType)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.UNAUTHORIZED_CLIENT,
                    "unauthorized grant type",
                    null
            ));
        }

        PreAuthCodePayload preAuthCodePayload = preAuthCodeService.consume(preAuthCodeGrantAuthentication.getPreAuthCode())
                .orElseThrow(() -> new OAuth2AuthenticationException(new OAuth2Error(
                        OAuth2ErrorCodes.INVALID_GRANT,
                        "pre_auth_code invalid",
                        null
                )));

        Set<String> authorizedScopes = resolveAuthorizedScopes(
                preAuthCodeGrantAuthentication.getScopes(),
                registeredClient.getScopes()
        );

        SecurityUser securityUser = preAuthCodePayload.toSecurityUser();
        UsernamePasswordAuthenticationToken principal = UsernamePasswordAuthenticationToken.authenticated(
                securityUser,
                null,
                securityUser.getAuthorities()
        );

        Map<String, Object> principalDetails = new LinkedHashMap<>();
        principalDetails.put(StandardClaimNamesConst.SID, preAuthCodePayload.sid());
        principalDetails.put(StandardClaimNamesConst.MFA, preAuthCodePayload.mfaVerified());
        principalDetails.put(StandardClaimNamesConst.CLIENT_TYPE, preAuthCodePayload.clientType() == null ? null : preAuthCodePayload.clientType().name());
        principalDetails.put(StandardClaimNamesConst.TENANT_ID, preAuthCodePayload.tenantId());
        principalDetails.put(StandardClaimNamesConst.JKT, preAuthCodePayload.dpopJkt());
        principal.setDetails(principalDetails);

        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(StringUtils.defaultIfBlank(securityUser.getUserId(), securityUser.getUsername()))
                .authorizationGrantType(preAuthCodeGrantType)
                .authorizedScopes(authorizedScopes)
                .attribute(Principal.class.getName(), principal)
                .attribute(OAuth2ParameterNames.SCOPE, String.join(" ", authorizedScopes));

        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(principal)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorization(authorizationBuilder.build())
                .authorizedScopes(authorizedScopes)
                .authorizationGrantType(preAuthCodeGrantType)
                .authorizationGrant(preAuthCodeGrantAuthentication)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.SERVER_ERROR,
                    "The token generator failed to generate the access token.",
                    null
            ));
        }
        OAuth2AccessToken accessToken = accessToken(authorizationBuilder, generatedAccessToken, tokenContext);

        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)
                && !ClientAuthenticationMethod.NONE.equals(clientPrincipal.getClientAuthenticationMethod())) {
            OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(principal)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .authorization(authorizationBuilder.build())
                    .authorizedScopes(authorizedScopes)
                    .authorizationGrantType(preAuthCodeGrantType)
                    .authorizationGrant(preAuthCodeGrantAuthentication)
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .build();

            OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(refreshTokenContext);
            if (generatedRefreshToken instanceof OAuth2RefreshToken token) {
                refreshToken = token;
                authorizationBuilder.refreshToken(refreshToken);
            }
        }

        OAuth2Authorization authorization = authorizationBuilder.build();
        this.authorizationService.save(authorization);

        Map<String, Object> additionalParameters = new LinkedHashMap<>();
        additionalParameters.put(StandardClaimNamesConst.SID, preAuthCodePayload.sid());
        additionalParameters.put(StandardClaimNamesConst.MFA, preAuthCodePayload.mfaVerified());
        additionalParameters.put(StandardClaimNamesConst.CLIENT_TYPE,
                preAuthCodePayload.clientType() == null ? ClientTypeEnum.WEB.name() : preAuthCodePayload.clientType().name());

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                clientPrincipal,
                accessToken,
                refreshToken,
                additionalParameters
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthCodeGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof OAuth2ClientAuthenticationToken clientAuthentication)
                || !clientAuthentication.isAuthenticated()) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_CLIENT,
                    "invalid client",
                    null
            ));
        }
        return clientAuthentication;
    }

    private Set<String> resolveAuthorizedScopes(Set<String> requestedScopes, Set<String> registeredScopes) {
        Set<String> scopes;
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            scopes = new LinkedHashSet<>(registeredScopes);
        } else {
            scopes = new LinkedHashSet<>(requestedScopes);
        }

        if (!registeredScopes.containsAll(scopes)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_SCOPE,
                    "scope is not allowed",
                    null
            ));
        }

        if (scopes.contains(OidcScopes.OPENID) && !registeredScopes.contains(OidcScopes.OPENID)) {
            throw new OAuth2AuthenticationException(new OAuth2Error(
                    OAuth2ErrorCodes.INVALID_SCOPE,
                    "openid scope is not allowed",
                    null
            ));
        }

        return scopes;
    }

    private ClientTypeEnum resolveClientType(PreAuthCodeGrantAuthenticationToken authenticationToken) {
        Object clientType = authenticationToken.getAdditionalParameters().get(StandardClaimNamesConst.CLIENT_TYPE);
        if (clientType instanceof String clientTypeString && StringUtils.isNotBlank(clientTypeString)) {
            try {
                return ClientTypeEnum.valueOf(clientTypeString.trim().toUpperCase());
            } catch (Exception ignored) {
                return ClientTypeEnum.WEB;
            }
        }
        return ClientTypeEnum.WEB;
    }

    private OAuth2AccessToken accessToken(OAuth2Authorization.Builder authorizationBuilder,
                                          OAuth2Token generatedAccessToken,
                                          OAuth2TokenContext tokenContext) {
        OAuth2AccessToken.TokenType tokenType = OAuth2AccessToken.TokenType.BEARER;
        if (generatedAccessToken instanceof ClaimAccessor claimAccessor) {
            Map<String, Object> cnf = claimAccessor.getClaimAsMap(StandardClaimNamesConst.CNF);
            if (cnf != null && cnf.containsKey(StandardClaimNamesConst.JKT)) {
                tokenType = OAuth2AccessToken.TokenType.DPOP;
            }
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                tokenType,
                generatedAccessToken.getTokenValue(),
                generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(),
                tokenContext.getAuthorizedScopes()
        );

        OAuth2TokenFormat accessTokenFormat = tokenContext
                .getRegisteredClient()
                .getTokenSettings()
                .getAccessTokenFormat();
        authorizationBuilder.token(accessToken, metadata -> {
            if (generatedAccessToken instanceof ClaimAccessor claimAccessor) {
                metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, claimAccessor.getClaims());
            }
            metadata.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, false);
            metadata.put(OAuth2TokenFormat.class.getName(), accessTokenFormat.getValue());
        });

        return accessToken;
    }
}
