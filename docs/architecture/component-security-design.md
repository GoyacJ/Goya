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
- 提供最小登录页：`GET /security/login`。
- 提供会话桥接接口：`POST /security/login/session`。
- 认证成功返回 `pre_auth_code`，风控要求二次认证时返回 `mfa_challenge_id`。
- 一次认证与二次认证均通过 `AuthenticationManager + AuthenticationProvider` 链执行。

### 2.3 security-oauth2

- 提供 OAuth2.1 授权服务器自动配置。
- 扩展授权类型：`grant_type=urn:goya:grant-type:pre-auth-code`。
- `pre_auth_code` 默认强绑定 `client_id`（`goya.security.oauth2.pre-auth.require-client-binding=true`）。
- 基于客户端类型解析 Access Token 格式（JWT/Opaque）。
- 注入统一 claims：`tenant_id`、`roles`、`authorities`、`client_type`、`sid`、`mfa`、`cnf.jkt`。
- 签名密钥改为 JDBC 持久化，默认 `P30D` 轮换，`P7D` 重叠验签窗口。
- 默认禁用内存 JWK 回退（`goya.security.oauth2.keys.allow-in-memory-fallback=false`）。
- 对 public client 强制开启 PKCE（`requireProofKey=true`）。

### 2.4 security-authorization

- 提供资源服务器自动配置。
- 支持 `AUTO/JWT/OPAQUE` 三种令牌校验模式。
- 已认证请求执行 `X-Tenant-Id/X-User-Id` 与 token claim 双向一致性校验（默认 `STRICT`）。
- `client_credentials` 机器令牌默认只做租户一致性校验，不强制 `X-User-Id`。
- API 授权资源码统一采用 `mappingCode`（`method + pathPattern`）并固定 action=`ACCESS`。
- 鉴权主体属性补齐 `roleIds/teamIds/orgIds`，用于 ROLE/TEAM/ORG 策略命中。
- 做吊销令牌校验、策略引擎联动。

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

## 4. 过滤器链顺序

- `@Order(1)` OAuth2 授权服务器链（`/oauth2/**`、`/.well-known/**`、`/connect/**`）
- `@Order(2)` 认证 API 链（`/api/security/auth/**`、`/security/login`）
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

## 6. 缓存键约定

- `goya:security:auth:attempt:{tenant}:{principal}:{ip}`
- `goya:security:auth:mfa:{challengeId}`
- `goya:security:auth:precode:{code}`
- `goya:security:token:revoked:{jti}`

## 7. 扩展点

业务方可按需覆盖：

- `IUserService`
- `ISocialUserService`
- `ITenantService`
- `IOtpService`
- `IRolePermissionService`
- `LoginRiskEvaluator`
