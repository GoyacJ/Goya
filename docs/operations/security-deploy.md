# Component Security 部署指南

## 1. 前置条件

- JDK 25（与仓库编译目标一致）
- 可用 Redis（认证临时态、MFA、预认证码、吊销索引）
- OAuth2 标准表所在数据库（`registered_client`、`authorization`、`consent`）
- OAuth2 JWK 持久化表（`oauth2_jwk`，参考 `component/component-security/security-oauth2/src/main/resources/db/oauth2_jwk.sql`）

## 2. 部署模式

### 2.1 单体内嵌（embedded）

适用：平台单体或网关+单认证服务场景。

建议启用模块：

- `security-authentication`
- `security-oauth2`
- `security-authorization`

示例配置：

```yaml
goya:
  security:
    authentication:
      enabled: true
    oauth2:
      enabled: true
      deployment-mode: EMBEDDED
      issuer: https://auth.example.com
      pre-auth:
        require-client-binding: true
      keys:
        allow-in-memory-fallback: false
    resource:
      enabled: true
      mode: AUTO
      consistency-mode: STRICT
      user-header: X-User-Id
      require-user-header-for-machine-token: false
      role-ids-claim: role_ids
      team-ids-claim: team_ids
      org-ids-claim: org_ids
      api-action: ACCESS
```

### 2.2 认证中心独立部署（auth-center）

适用：统一 SSO，多业务资源服务分离。

认证中心启用：

- `security-authentication`
- `security-oauth2`

资源服务启用：

- `security-authorization`

认证中心示例：

```yaml
goya:
  security:
    authentication:
      enabled: true
    oauth2:
      enabled: true
      deployment-mode: AUTH_CENTER
      issuer: https://auth.example.com
      pre-auth:
        require-client-binding: true
      keys:
        rotation-interval: P30D
        overlap: P7D
        allow-in-memory-fallback: false
```

资源服务示例：

```yaml
goya:
  security:
    resource:
      enabled: true
      mode: AUTO
      issuer-uri: https://auth.example.com
      introspection-uri: https://auth.example.com/oauth2/introspect
      introspection-client-id: resource-api
      introspection-client-secret: ${RESOURCE_INTROSPECTION_SECRET}
```

## 3. 客户端建议

- Web 客户端：默认 JWT Access Token
- 移动端、小程序：默认 Opaque Access Token
- 公开客户端（public client）：必须启用 PKCE
- 禁用隐式授权（OAuth2.1）
- Web SSO 登录页需在拿到 `pre_auth_code` 后调用 `POST /security/login/session` 建立服务端会话。

## 4. 社交登录与小程序登录

- 需启用 `component-social` 并正确配置第三方应用参数。
- 社交账号绑定关系通过 `ISocialUserService` 提供。

## 5. SPI 接入清单

至少实现：

- `IUserService`

按能力实现：

- `ITenantService`
- `IOtpService`
- `ISocialUserService`
- `IRolePermissionService`
- `LoginRiskEvaluator`（可选）

## 6. 验收命令

```bash
mvn -pl component/component-security/security-core -am -DskipTests validate
mvn -pl component/component-security/security-authentication -am -DskipTests validate
mvn -pl component/component-security/security-oauth2 -am -DskipTests validate
mvn -pl component/component-security/security-authorization -am -DskipTests validate
mvn -pl component/component-security -am -DskipTests validate
```

全仓编译请在 JDK 25 环境执行：

```bash
mvn -DskipTests compile
```

## 7. 迁移步骤（破坏性变更）

1. 执行 `oauth2_jwk` 建表 SQL。
2. 回填 API 资源到 `data_resource`，并将 API 策略 `action` 统一迁移为 `ACCESS`。
3. 将 API 策略 `resource_code` 迁移为 `mappingCode`（`method:pathPattern`）。
4. 升级认证请求：密码/短信/小程序登录请求增加 `clientId`，社交回调透传 `client_id`。
5. 升级所有已认证用户请求：必须同时发送 `X-Tenant-Id` 与 `X-User-Id`，且与 token claim 完全一致（机器令牌默认豁免 `X-User-Id`）。
6. 发布资源服务严格模式，再发布认证 Provider 化与 OAuth2 密钥持久化。

## 8. 常见问题

- 资源服务 `AUTO` 模式鉴权失败：确认 `issuer-uri` 或 `jwk-set-uri` / introspection 三元组已配置。
- 令牌无法换取：确认客户端已启用 `urn:goya:grant-type:pre-auth-code` 授权类型。
- `pre_auth_code` 交换失败（client mismatch）：检查认证请求与 `/oauth2/token` 使用的 `client_id` 是否一致。
- 社交回调失败：检查第三方配置、回调地址与 `ISocialUserService` 绑定逻辑。
- 一致性校验 403：检查 `X-Tenant-Id/X-User-Id` 是否存在且与 token 的 `tenant_id/sub` 一致。
