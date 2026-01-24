package com.ysmjjsy.goya.component.security.oauth2.service.adapter;

import com.ysmjjsy.goya.component.security.oauth2.service.IOAuth2AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

/**
 * <p>OAuth2AuthorizationService 适配器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@RequiredArgsConstructor
public class OAuth2AuthorizationServiceAdapter implements OAuth2AuthorizationService {

    private final IOAuth2AuthorizationService authorizationService;

    @Override
    public void save(OAuth2Authorization authorization) {
        authorizationService.save(authorization);
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        authorizationService.remove(authorization);
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return authorizationService.findById(id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        return authorizationService.findByToken(token, tokenType);
    }
}
