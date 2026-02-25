# security-oauth2

`security-oauth2` 提供 OAuth2.1 授权服务器能力、扩展授权类型和令牌生命周期治理。

## 核心能力

- OAuth2.1 授权服务器自动配置
- 扩展授权类型：`urn:goya:grant-type:pre-auth-code`
- PKCE 强制策略（public client）
- Access Token 格式策略解析（按客户端类型）
- 双 Token 默认策略：
  - Access Token：JWT（Web / 移动端 / 小程序默认）
  - Refresh Token：Opaque（默认轮换）
- JDBC JWK 持久化与轮换

## 会话与撤销闭环

- `RevocationIndexingAuthorizationService` 写入撤销索引：
  - `sid -> tokenIds`
  - `sid -> authorizationIds`
  - `tenant:user -> authorizationIds`
  - `tenant:user:client -> authorizationIds`
- `OAuth2SecuritySessionLifecycleService` 支持：
  - `logoutCurrent`
  - `revokeBySid`
  - `revokeByUser`
  - `revokeByUserAndClient`
- `OidcLogoutRevocationFilter` 将 `/connect/logout` 收敛到统一会话撤销服务。

## 关键配置

配置前缀：`goya.security.oauth2`

- `pre-auth.require-client-binding`
- `allow-refresh-token-for-public-clients`
- `web-access-token-format`
- `mobile-app-access-token-format`
- `mini-program-access-token-format`
- `reuse-refresh-tokens`
- `keys.rotation-interval`
- `keys.overlap`
- `keys.allow-in-memory-fallback`

## 构建校验

```bash
mvn -pl component/component-security/security-oauth2 -am -DskipTests validate
```
