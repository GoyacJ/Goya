//package com.ysmjjsy.goya.component.security.authentication.service;
//
//import com.ysmjjsy.goya.component.security.authentication.domain.SecurityRegisteredClient;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.security.oauth2.core.AuthorizationGrantType;
//import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
//import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
//import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
//
//import java.util.Set;
//
///**
// * <p>基于数据库的RegisteredClientRepository实现</p>
// * <p>实现Spring Authorization Server的RegisteredClientRepository接口</p>
// * <p>支持RegisteredClient与RegisteredClientEntity的相互转换</p>
// *
// * <p>参考文档：</p>
// * <ul>
// *   <li><a href="https://github.com/spring-projects/spring-authorization-server/blob/main/docs/modules/ROOT/pages/guides/how-to-jpa.adoc">Spring Authorization Server JPA Guide</a></li>
// * </ul>
// *
// * @author goya
// * @since 2026/1/5
// */
//@Slf4j
//@RequiredArgsConstructor
//public class DatabaseRegisteredClientRepository implements RegisteredClientRepository {
//
//    private final IRegisteredClientMapper registeredClientMapper;
//
//    @Override
//    public void save(RegisteredClient registeredClient) {
//        SecurityRegisteredClient entity = toEntity(registeredClient);
//
//        // 检查是否已存在（通过clientId查找）
//        SecurityRegisteredClient existing = registeredClientMapper.selectOne(
//                new LambdaQueryWrapper<SecurityRegisteredClient>()
//                        .eq(SecurityRegisteredClient::getClientId, registeredClient.getClientId())
//        );
//        if (existing != null) {
//            // 更新
//            entity.setId(existing.getId());
//            registeredClientMapper.updateById(entity);
//            log.debug("[Goya] |- security [core] RegisteredClient updated: {}", registeredClient.getClientId());
//        } else {
//            // 新增
//            registeredClientMapper.insert(entity);
//            log.debug("[Goya] |- security [core] RegisteredClient saved: {}", registeredClient.getClientId());
//        }
//    }
//
//    @Override
//    public RegisteredClient findById(String id) {
//        SecurityRegisteredClient entity = registeredClientMapper.selectById(id);
//        if (entity == null) {
//            return null;
//        }
//        return toRegisteredClient(entity);
//    }
//
//    @Override
//    public RegisteredClient findByClientId(String clientId) {
//        SecurityRegisteredClient entity = registeredClientMapper.selectOne(
//                new LambdaQueryWrapper<SecurityRegisteredClient>()
//                        .eq(SecurityRegisteredClient::getClientId, clientId)
//        );
//        if (entity == null) {
//            return null;
//        }
//        return toRegisteredClient(entity);
//    }
//
//    /**
//     * 将RegisteredClient转换为RegisteredClientEntity
//     *
//     * @param registeredClient RegisteredClient对象
//     * @return RegisteredClientEntity对象
//     */
//    private SecurityRegisteredClient toEntity(RegisteredClient registeredClient) {
//        return SecurityRegisteredClient.builder()
//                .id(registeredClient.getId())
//                .clientId(registeredClient.getClientId())
//                .clientIdIssuedAt(registeredClient.getClientIdIssuedAt())
//                .clientSecret(registeredClient.getClientSecret())
//                .clientSecretExpiresAt(registeredClient.getClientSecretExpiresAt())
//                .previousClientSecret(null) // 密钥轮换字段，在轮换时设置
//                .secretRotationTime(null) // 密钥轮换时间，在轮换时设置
//                .clientName(registeredClient.getClientName())
//                .clientAuthenticationMethods(JsonUtils.toJson(registeredClient.getClientAuthenticationMethods()))
//                .authorizationGrantTypes(JsonUtils.toJson(registeredClient.getAuthorizationGrantTypes()))
//                .redirectUris(JsonUtils.toJson(registeredClient.getRedirectUris()))
//                .postLogoutRedirectUris(JsonUtils.toJson(registeredClient.getPostLogoutRedirectUris()))
//                .scopes(JsonUtils.toJson(registeredClient.getScopes()))
//                .clientSettings(JsonUtils.toJson(registeredClient.getClientSettings()))
//                .tokenSettings(JsonUtils.toJson(registeredClient.getTokenSettings()))
//                .build();
//    }
//
//    /**
//     * 将RegisteredClientEntity转换为RegisteredClient
//     *
//     * @param entity RegisteredClientEntity对象
//     * @return RegisteredClient对象
//     */
//    private RegisteredClient toRegisteredClient(SecurityRegisteredClient entity) {
//        RegisteredClient.Builder builder = RegisteredClient.withId(entity.getId())
//                .clientId(entity.getClientId())
//                .clientName(entity.getClientName());
//
//        // 设置客户端ID签发时间
//        if (entity.getClientIdIssuedAt() != null) {
//            builder.clientIdIssuedAt(entity.getClientIdIssuedAt());
//        }
//
//        // 设置客户端密钥
//        if (entity.getClientSecret() != null) {
//            builder.clientSecret(entity.getClientSecret());
//            if (entity.getClientSecretExpiresAt() != null) {
//                builder.clientSecretExpiresAt(entity.getClientSecretExpiresAt());
//            }
//        }
//
//        // 设置客户端认证方法
//        if (entity.getClientAuthenticationMethods() != null) {
//            Set<String> methodStrings = JsonUtils.fromJsonSet(
//                    entity.getClientAuthenticationMethods(),
//                    String.class
//            );
//            if (methodStrings != null) {
//                methodStrings.stream()
//                        .map(ClientAuthenticationMethod::new)
//                        .forEach(builder::clientAuthenticationMethod);
//            }
//        }
//
//        // 设置授权类型
//        if (entity.getAuthorizationGrantTypes() != null) {
//            Set<String> grantTypeStrings = JsonUtils.fromJsonSet(
//                    entity.getAuthorizationGrantTypes(),
//                    String.class
//            );
//            if (grantTypeStrings != null) {
//                grantTypeStrings.stream()
//                        .map(AuthorizationGrantType::new)
//                        .forEach(builder::authorizationGrantType);
//            }
//        }
//
//        // 设置重定向URI
//        if (entity.getRedirectUris() != null) {
//            Set<String> redirectUris = JsonUtils.fromJsonSet(
//                    entity.getRedirectUris(),
//                    String.class
//            );
//            if (redirectUris != null) {
//                redirectUris.forEach(builder::redirectUri);
//            }
//        }
//
//        // 设置登出后重定向URI
//        if (entity.getPostLogoutRedirectUris() != null) {
//            Set<String> postLogoutRedirectUris = JsonUtils.fromJsonSet(
//                    entity.getPostLogoutRedirectUris(),
//                    String.class
//            );
//            if (postLogoutRedirectUris != null) {
//                postLogoutRedirectUris.forEach(builder::postLogoutRedirectUri);
//            }
//        }
//
//        // 设置授权范围
//        if (entity.getScopes() != null) {
//            Set<String> scopes = JsonUtils.fromJsonSet(
//                    entity.getScopes(),
//                    String.class
//            );
//            if (scopes != null) {
//                scopes.forEach(builder::scope);
//            }
//        }
//
//        // 设置客户端设置
//        if (entity.getClientSettings() != null) {
//            ClientSettings clientSettings = JsonUtils.fromJson(
//                    entity.getClientSettings(),
//                    ClientSettings.class
//            );
//            builder.clientSettings(clientSettings);
//        }
//
//        // 设置Token设置
//        if (entity.getTokenSettings() != null) {
//            TokenSettings tokenSettings = JsonUtils.fromJson(
//                    entity.getTokenSettings(),
//                    TokenSettings.class
//            );
//            builder.tokenSettings(tokenSettings);
//        }
//
//        return builder.build();
//    }
//}
//
