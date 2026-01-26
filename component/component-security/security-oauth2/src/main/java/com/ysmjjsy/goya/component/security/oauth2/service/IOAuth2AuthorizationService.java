package com.ysmjjsy.goya.component.security.oauth2.service;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

/**
 * <p>OAuth2Authorization 服务SPI</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface IOAuth2AuthorizationService {

    void save(OAuth2Authorization authorization);

    void remove(OAuth2Authorization authorization);

    OAuth2Authorization findById(String id);

    OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType);
}
