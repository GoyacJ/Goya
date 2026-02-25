package com.ysmjjsy.goya.component.security.oauth2.service;

import com.ysmjjsy.goya.component.security.core.enums.ClientTypeEnum;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

/**
 * <p>令牌格式解析器</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SecurityOAuth2TokenFormatResolver {

    public static final String CLIENT_TYPE_SETTING = "goya.client_type";
    public static final String ACCESS_TOKEN_FORMAT_SETTING = "goya.access_token_format";

    private final SecurityOAuth2Properties securityOAuth2Properties;

    public SecurityOAuth2TokenFormatResolver(SecurityOAuth2Properties securityOAuth2Properties) {
        this.securityOAuth2Properties = securityOAuth2Properties;
    }

    public RegisteredClient resolve(RegisteredClient registeredClient, ClientTypeEnum clientType) {
        if (registeredClient == null) {
            return null;
        }

        ClientTypeEnum finalClientType = resolveClientType(registeredClient, clientType);
        OAuth2TokenFormat targetAccessTokenFormat = resolveAccessTokenFormat(registeredClient, finalClientType);

        TokenSettings existingTokenSettings = registeredClient.getTokenSettings();
        TokenSettings tokenSettings = TokenSettings.withSettings(existingTokenSettings.getSettings())
                .accessTokenFormat(targetAccessTokenFormat)
                .accessTokenTimeToLive(targetAccessTokenFormat.equals(OAuth2TokenFormat.REFERENCE)
                        ? securityOAuth2Properties.accessTokenTtlOpaque()
                        : securityOAuth2Properties.accessTokenTtlJwt())
                .refreshTokenTimeToLive(securityOAuth2Properties.refreshTokenTtl())
                .reuseRefreshTokens(securityOAuth2Properties.reuseRefreshTokens())
                .build();

        return RegisteredClient.from(registeredClient)
                .tokenSettings(tokenSettings)
                .build();
    }

    private OAuth2TokenFormat resolveAccessTokenFormat(RegisteredClient registeredClient, ClientTypeEnum clientType) {
        Object setting = registeredClient.getClientSettings().getSetting(ACCESS_TOKEN_FORMAT_SETTING);
        if (setting instanceof String configuredFormat && StringUtils.isNotBlank(configuredFormat)) {
            return parseTokenFormat(configuredFormat);
        }

        return switch (clientType) {
            case MOBILE_APP, DESKTOP_APP -> parseTokenFormat(securityOAuth2Properties.mobileAppAccessTokenFormat());
            case MINIPROGRAM -> parseTokenFormat(securityOAuth2Properties.miniProgramAccessTokenFormat());
            case WEB -> parseTokenFormat(securityOAuth2Properties.webAccessTokenFormat());
        };
    }

    private ClientTypeEnum resolveClientType(RegisteredClient registeredClient, ClientTypeEnum requestClientType) {
        if (requestClientType != null) {
            return requestClientType;
        }

        Object setting = registeredClient.getClientSettings().getSetting(CLIENT_TYPE_SETTING);
        if (setting instanceof String configuredClientType && StringUtils.isNotBlank(configuredClientType)) {
            try {
                return ClientTypeEnum.valueOf(configuredClientType.trim().toUpperCase());
            } catch (Exception ignored) {
                return ClientTypeEnum.WEB;
            }
        }

        return ClientTypeEnum.WEB;
    }

    private OAuth2TokenFormat parseTokenFormat(String value) {
        if (StringUtils.isBlank(value)) {
            return OAuth2TokenFormat.SELF_CONTAINED;
        }

        String normalized = value.trim().toUpperCase();
        if ("OPAQUE".equals(normalized) || "REFERENCE".equals(normalized)) {
            return OAuth2TokenFormat.REFERENCE;
        }
        return OAuth2TokenFormat.SELF_CONTAINED;
    }
}
