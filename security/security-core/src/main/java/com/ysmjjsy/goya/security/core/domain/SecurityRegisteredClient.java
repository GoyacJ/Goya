package com.ysmjjsy.goya.security.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * <p>OAuth2注册客户端实体</p>
 * <p>用于数据库存储RegisteredClient信息</p>
 * <p>参考Spring Authorization Server的RegisteredClient结构</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2注册客户端实体")
public class SecurityRegisteredClient {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "客户端ID签发时间")
    private Instant clientIdIssuedAt;

    @Schema(description = "客户端密钥")
    private String clientSecret;

    @Schema(description = "客户端密钥过期时间")
    private Instant clientSecretExpiresAt;

    @Schema(description = "旧客户端密钥（用于密钥轮换过渡期）")
    private String previousClientSecret;

    @Schema(description = "密钥轮换时间")
    private Instant secretRotationTime;

    @Schema(description = "客户端名称")
    private String clientName;

    @Schema(description = "客户端认证方法（JSON格式，存储Set<ClientAuthenticationMethod>）")
    private String clientAuthenticationMethods;

    @Schema(description = "授权类型（JSON格式，存储Set<AuthorizationGrantType>）")
    private String authorizationGrantTypes;

    @Schema(description = "重定向URI（JSON格式，存储Set<String>）")
    private String redirectUris;

    @Schema(description = "登出后重定向URI（JSON格式，存储Set<String>）")
    private String postLogoutRedirectUris;

    @Schema(description = "授权范围（JSON格式，存储Set<String>）")
    private String scopes;

    @Schema(description = "客户端设置（JSON格式，存储ClientSettings）")
    private String clientSettings;

    @Schema(description = "Token设置（JSON格式，存储TokenSettings）")
    private String tokenSettings;
}

