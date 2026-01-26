package com.ysmjjsy.goya.component.security.oauth2.service;

import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;

/**
 * <p>OAuth2AuthorizationConsent 服务SPI</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface IOAuth2AuthorizationConsentService {

    void save(OAuth2AuthorizationConsent authorizationConsent);

    void remove(OAuth2AuthorizationConsent authorizationConsent);

    OAuth2AuthorizationConsent findById(String registeredClientId, String principalName);
}
