package com.ysmjjsy.goya.component.security.oauth2.configuration.security;

import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;

/**
 * <p>公开客户端 PKCE 强制仓储包装器。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class PkceEnforcingRegisteredClientRepository implements RegisteredClientRepository {

    private final RegisteredClientRepository delegate;
    private final boolean requirePkceForPublicClients;

    public PkceEnforcingRegisteredClientRepository(RegisteredClientRepository delegate,
                                                   boolean requirePkceForPublicClients) {
        this.delegate = delegate;
        this.requirePkceForPublicClients = requirePkceForPublicClients;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        delegate.save(enforcePkce(registeredClient));
    }

    @Override
    public RegisteredClient findById(String id) {
        return enforcePkce(delegate.findById(id));
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return enforcePkce(delegate.findByClientId(clientId));
    }

    private RegisteredClient enforcePkce(RegisteredClient registeredClient) {
        if (!requirePkceForPublicClients || registeredClient == null || !isPublicClient(registeredClient)) {
            return registeredClient;
        }

        if (Boolean.TRUE.equals(registeredClient.getClientSettings().isRequireProofKey())) {
            return registeredClient;
        }

        ClientSettings clientSettings = ClientSettings.withSettings(registeredClient.getClientSettings().getSettings())
                .requireProofKey(true)
                .build();

        return RegisteredClient.from(registeredClient)
                .clientSettings(clientSettings)
                .build();
    }

    private boolean isPublicClient(RegisteredClient registeredClient) {
        return registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE);
    }
}

