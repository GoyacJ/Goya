# Component Security 设计说明

## 1. 目标与边界

`component-security` 负责认证授权框架能力，不承载业务账号域数据。业务方通过 SPI 注入用户、租户、社交绑定、OTP 与权限查询能力。

支持能力：

- Web / 移动端 / 小程序统一认证入口
- 基于 Spring Security `AuthenticationProvider` 的认证编排
- OAuth2.1 授权服务器（含 `pre-auth-code` 扩展授权类型）
- 资源服务器 JWT / Opaque 混合校验
- SSO 登录与登出链路
- 前后端分离场景下的最小 Web 登录页

不在本组件范围：

- 业务用户体系实现
- 业务租户策略
- 业务品牌 UI

## 2. 模块职责

### 2.1 security-core

- 提供 `GoyaSecurityContext`，实现 `currentUser/currentTenant` 解析。
- 提供 SPI：`IUserService`、`ISocialUserService`、`ITenantService`、`IOtpService`、`IRolePermissionService`。
- 提供风险评估扩展点：`LoginRiskEvaluator`（可选覆盖）。
- 提供会话生命周期抽象：`SecuritySessionLifecycleService`，统一 `logoutCurrent/revokeBySid/revokeByUser/revokeByUserAndClient`。
- 提供会话撤销结果模型：`SecuritySessionRevocationResult`。
- 提供错误码治理：`SecurityErrorCode`、`SecurityErrorCodeCatalog`。

### 2.2 security-authentication

- 提供统一认证 API：
  - `POST /api/security/auth/password/login`
  - `POST /api/security/auth/sms/send`
  - `POST /api/security/auth/sms/login`
  - `GET /api/security/auth/social/{source}/authorize`
  - `GET /api/security/auth/social/{source}/callback`
  - `POST /api/security/auth/wx-mini/login`
  - `POST /api/security/auth/mfa/challenge`
  - `POST /api/security/auth/mfa/verify`
  - `POST /api/security/auth/logout`（`CURRENT_SESSION | ALL_SESSIONS | BY_CLIENT`）
  - `POST /api/security/auth/kickout`（管理员按 `tenantId/userId/clientId` 踢出）
- 提供最小登录页：`GET /security/login`。
- 提供会话桥接接口：`POST /security/login/session`。
- 对外 `logout/kickout` 命令统一委托 `SecuritySessionCommandService -> SecuritySessionLifecycleService`。
- 认证成功返回 `pre_auth_code`，风控要求二次认证时返回 `mfa_challenge_id`。
- 一次认证与二次认证均通过 `AuthenticationManager + AuthenticationProvider` 链执行。
- 提供默认 `ISocialUserService` 适配器（`@ConditionalOnMissingBean`），复用 `component-social` 的 `SocialBindingStore`。

### 2.3 security-oauth2

- 提供 OAuth2.1 授权服务器自动配置。
- 扩展授权类型：`grant_type=urn:goya:grant-type:pre-auth-code`。
- `pre_auth_code` 默认强绑定 `client_id`（`goya.security.oauth2.pre-auth.require-client-binding=true`）。
- 基于客户端类型解析 Access Token 格式（JWT/Opaque）。
- 默认令牌策略：`AccessToken=JWT`（Web/移动端/小程序默认一致），`RefreshToken=Opaque`。
- `refresh_token` 默认轮换（`reuse=false`），支持公开客户端按配置签发（`allow-refresh-token-for-public-clients`）。
- 注入统一 claims：`tenant_id`、`roles`、`authorities`、`client_type`、`sid`、`mfa`、`cnf.jkt`。
- 签名密钥改为 JDBC 持久化，默认 `P30D` 轮换，`P7D` 重叠验签窗口。
- 默认禁用内存 JWK 回退（`goya.security.oauth2.keys.allow-in-memory-fallback=false`）。
- 对 public client 强制开启 PKCE（`requireProofKey=true`）。
- 授权存储写入撤销索引：
  - `sid -> tokenIds`
  - `sid -> authorizationIds`
  - `tenant:user -> authorizationIds`
  - `tenant:user:client -> authorizationIds`
- `/connect/logout` 通过 `OidcLogoutRevocationFilter` 收敛到统一会话撤销服务。

### 2.4 security-authorization

- 提供资源服务器自动配置。
- 支持 `AUTO/JWT/OPAQUE` 三种令牌校验模式。
- 已认证请求执行 `X-Tenant-Id/X-User-Id` 与 token claim 双向一致性校验（默认 `STRICT`）。
- `client_credentials` 机器令牌默认只做租户一致性校验，不强制 `X-User-Id`。
- API 授权资源码统一采用 `mappingCode`（`method + pathPattern`）并固定 action=`ACCESS`。
- 鉴权主体属性补齐 `roleIds/teamIds/orgIds`，用于 ROLE/TEAM/ORG 策略命中。
- `RevokedTokenFilter` 作为资源侧最终吊销拦截，撤销后请求即时返回 401。
- `PolicyAuthorizationFilter` 与策略引擎联动，策略变更后即时生效。
- 内置安全端点 `POST /api/security/auth/logout|kickout` 默认绕过策略判定，仅要求认证并执行会话命令权限校验。

## 3. 关键流程

### 3.1 Web SSO

1. 客户端访问 `/oauth2/authorize`。
2. 跳转 `/security/login`。
3. 调用认证 API 完成一次认证与可选 MFA。
4. 认证模块签发 `pre_auth_code`。
5. 调用 `POST /security/login/session` 建立服务端会话并回跳授权请求。
6. 继续完成授权码流程并换取 token。

### 3.2 移动端 / 小程序

1. 直接调用认证 API。
2. 获取 `pre_auth_code`。
3. 调用 `/oauth2/token`（`urn:goya:grant-type:pre-auth-code`）换 token。

### 3.3 资源访问

1. 资源服务按模式解析 JWT/Opaque。
2. 校验 `X-Tenant-Id/X-User-Id` 与 token claim（默认严格一致）。
3. 映射 `roles/authorities` 到 `GrantedAuthority`。
4. 解析 `bestMatchingPattern + method` 生成 `mappingCode`，以 `ACCESS` 动作走策略引擎判定。

### 3.4 会话撤销闭环

1. 用户调用 `POST /api/security/auth/logout`：按 scope 触发当前会话/同用户全端/同用户同客户端撤销。
2. 管理员调用 `POST /api/security/auth/kickout`：按 `tenantId/userId/clientId` 撤销目标会话。
3. OIDC RP-Initiated Logout 调用 `/connect/logout`：同样进入 `SecuritySessionLifecycleService`。
4. `OAuth2AuthorizationService.remove` 触发 access/refresh 吊销标记写入。
5. 资源侧 `RevokedTokenFilter` 立即拦截旧 token（401）。

## 4. 过滤器链顺序

- `@Order(1)` OAuth2 授权服务器链（`/oauth2/**`、`/.well-known/**`、`/connect/**`）
- `@Order(2)` 认证 API 链（登录/MFA/社交/小程序 + `/security/login`）
- `@Order(3)` 资源访问链（业务 API）

## 5. 配置与默认值

- `goya.security.authentication.pre-auth-code-ttl = 60s`
- `goya.security.authentication.mfa-challenge-ttl = 300s`
- 登录失败阈值：5 次 / 15 分钟
- 锁定时间：15 分钟
- 默认租户：`public`
- `goya.security.resource.consistency-mode = STRICT`
- `goya.security.resource.user-header = X-User-Id`
- `goya.security.resource.require-user-header-for-machine-token = false`
- `goya.security.resource.role-ids-claim = role_ids`
- `goya.security.resource.team-ids-claim = team_ids`
- `goya.security.resource.org-ids-claim = org_ids`
- `goya.security.resource.api-action = ACCESS`
- Access Token TTL：JWT 15 分钟，Opaque 30 分钟
- Refresh Token TTL：14 天，`reuse=false`
- 密钥轮换：`goya.security.oauth2.keys.rotation-interval = P30D`，`goya.security.oauth2.keys.overlap = P7D`
- `goya.security.oauth2.keys.allow-in-memory-fallback = false`
- `goya.security.oauth2.pre-auth.require-client-binding = true`
- `goya.security.oauth2.allow-refresh-token-for-public-clients = true`

## 6. 缓存键约定

- `goya:security:auth:attempt:{tenant}:{principal}:{ip}`
- `goya:security:auth:mfa:{challengeId}`
- `goya:security:auth:precode:{code}`
- `goya:security:token:revoked:{jti}`
- `goya:security:sso:sid:{sid} -> tokenIds`
- `goya:security:sso:sid:auth:{sid} -> authorizationIds`
- `goya:security:sso:user:auth:{tenant}:{user} -> authorizationIds`
- `goya:security:sso:user-client:auth:{tenant}:{user}:{client} -> authorizationIds`

## 7. 与数据权限联动

- 动态 API 权限：`PolicyAuthorizationFilter` 基于运行时策略即时判定，无需重启生效。
- 数据权限主链：`component-mybatisplus` 的 `GoyaDataPermissionHandler`。
- 生产建议：保持 `goya.mybatis-plus.permission.fail-closed=true`，策略/上下文异常时按 `1=0` 收敛。

## 8. 扩展点

业务方可按需覆盖：

- `IUserService`
- `ISocialUserService`
- `ITenantService`
- `IOtpService`
- `IRolePermissionService`
- `LoginRiskEvaluator`
