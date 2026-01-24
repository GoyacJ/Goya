package com.ysmjjsy.goya.component.security.oauth2.service;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

/**
 * <p>RegisteredClient 服务SPI</p>
 *
 * @author goya
 * @since 2026/1/5
 */
public interface IRegisteredClientService {

    void save(RegisteredClient registeredClient);

    RegisteredClient findById(String id);

    RegisteredClient findByClientId(String clientId);
}
