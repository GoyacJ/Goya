package com.ysmjjsy.goya.component.security.oauth2.service.adapter;

import com.ysmjjsy.goya.component.security.oauth2.service.IOAuth2AuthorizationConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsent;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;

/**
 * <p>OAuth2AuthorizationConsentService 适配器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@RequiredArgsConstructor
public class OAuth2AuthorizationConsentServiceAdapter implements OAuth2AuthorizationConsentService {

    private final IOAuth2AuthorizationConsentService consentService;

    @Override
    public void save(OAuth2AuthorizationConsent authorizationConsent) {
        consentService.save(authorizationConsent);
    }

    @Override
    public void remove(OAuth2AuthorizationConsent authorizationConsent) {
        consentService.remove(authorizationConsent);
    }

    @Override
    public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
        return consentService.findById(registeredClientId, principalName);
    }
}
