package com.ysmjjsy.goya.component.security.oauth2.service.adapter;

import com.ysmjjsy.goya.component.security.oauth2.service.IRegisteredClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

/**
 * <p>RegisteredClientRepository 适配器</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@RequiredArgsConstructor
public class RegisteredClientRepositoryAdapter implements RegisteredClientRepository {

    private final IRegisteredClientService registeredClientService;

    @Override
    public void save(RegisteredClient registeredClient) {
        registeredClientService.save(registeredClient);
    }

    @Override
    public RegisteredClient findById(String id) {
        return registeredClientService.findById(id);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return registeredClientService.findByClientId(clientId);
    }
}
