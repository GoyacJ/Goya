# 企业级认证授权服务技术方案

> 技术栈：Spring Boot 4.0.1 · Spring Security 7 · Spring Authorization Server
> 目标：构建可落地的企业级认证授权平台（OAuth2.1 + OIDC），支持多端接入、业务系统无 Session、多登录方式、SSO、多租户，Access Token 为 JWT，Refresh Token 为 Opaque。

## 0. 约束与边界

- component-security 只提供安全能力与 SPI 接口，不实现 DB 层（无 DBA）。
- IAM / OAuth2 相关持久化服务由业务实现并注入。
- 方案以“认证授权中心 + 资源服务 + IAM 域”分层。
- Auth Server 载体采用 `component-security/security-oauth2` 模块。
- 业务 API 必须无 Session；Auth Server 可保留登录会话用于 SSO，并建议使用 Spring Session + Redis 外置。

## 1. 建设目标

- 统一认证授权中心（OAuth2.1 + OIDC）。
- 支持 PC/Web/移动端，提供 SSO。
- 资源服务无 Session，仅验证 Bearer Token。
- 登录方式可插拔：用户名密码、手机号 OTP、第三方登录。
- 多租户隔离：issuer / JWK / 客户端 / 授权数据。
- Token 策略：JWT Access Token + Opaque Refresh Token。

## 2. 总体架构与模块边界

**逻辑分层**

- Auth Server（授权服务器）
  - OIDC 端点、OAuth2.1 授权流程
  - 登录入口与 SSO
  - Token 签发与刷新
- IAM Domain（业务实现）
  - 用户、租户、角色权限
  - 设备、审计、风控
- Resource Server（资源服务器）
  - JWT 校验
  - Scope/Role 鉴权

**在本项目中的模块落位**

- `component-security/security-core`
  - 领域模型（SecurityUser / SecurityPermission 等）
  - 常量、配置前缀
  - IAM SPI（IUserService 等）
- `component-security/security-authentication`
  - 认证方式适配（密码/短信/社交登录）
  - 验证码、风控、失败处理
- `component-security/security-authorization`
  - Resource Server JWT 验证、黑名单、DPoP、多租户解析
- `component-security/security-oauth2`（新增，Auth Server 载体）
  - Authorization Server 相关组件
  - Token 定制、租户 issuer 解析
  - OAuth2 授权与存储 SPI

> 注：security-authorization 作为资源服务能力保留，Auth Server 独立在 security-oauth2 模块，保持职责清晰。

## 3. 协议与核心端点

**OIDC/OAuth2.1 端点**

- `/.well-known/openid-configuration`（按租户路径暴露：`/t/{tenant}/.well-known/openid-configuration`）
- `/oauth2/authorize`
- `/oauth2/token`
- `/oauth2/jwks`
- `/userinfo`
- `/oauth2/revoke`
- `/oauth2/introspect`
- `/connect/logout`

**授权模式（Grant Types）**

- Authorization Code + PKCE（PC/Web/SPA/移动端默认）
- Refresh Token
- Client Credentials（服务间调用）

## 4. 多端接入规范

- Web/PC：Authorization Code + PKCE + BFF
- SPA：Authorization Code + PKCE，Access Token 仅内存存储
- Mobile：Authorization Code + PKCE，系统浏览器授权
- Service-to-service：Client Credentials

## 5. Token 体系设计

**Access Token（JWT）**

- 5–15 分钟有效期
- RSA 签名
- Claim 建议：`iss` `sub` `aud` `exp` `iat` `jti` `tenant_id` `client_id` `scope` `roles` `authorities`

**Refresh Token（Opaque）**

- 7–30 天有效期
- 数据库存储哈希值（由业务实现）
- Rotation + Revoke

**撤销策略**

- 黑名单缓存（resource 侧已有 JwtBlacklistValidator）
- `oauth2/revoke` + refresh token rotation

## 6. 多租户设计

- 租户识别：Path 为主（`/t/{tenant}`），预留扩展 Subdomain
- 多 issuer：`https://auth.example.com/t/{tenant}`
- 每租户独立：JWK、RegisteredClient、Authorization 数据
- Resource Server：从 token claim 或请求上下文确定 tenant

## 7. 登录与认证方式

- 用户名密码（BCrypt）
- 手机号 OTP（短信登录）
- 第三方登录（OIDC/OAuth2 Provider）

**统一认证入口**

- 自定义 AuthenticationToken + AuthenticationProvider
- 登录成功统一转换为 OAuth2 授权流程
- 短信/第三方登录仅作为登录步骤，不新增 grant 类型

## 8. 权限模型与鉴权策略

- RBAC：User / Role / Permission
- JWT `roles` → GrantedAuthority
- OAuth2 Scope → API 访问控制
- 细粒度鉴权：由业务系统二次校验

## 9. IAM 与 OAuth2 SPI 设计（仅接口，无 DB）

参考 `IUserService` 的定义方式，建议新增以下接口（业务侧实现）：

**IAM 相关**

- `ITenantService`
  - 解析租户、租户配置（issuer、开关、策略）
- `IRolePermissionService`
  - 读取用户角色与权限
- `IOtpService`
  - 发送/校验短信或邮箱 OTP
- `ISocialUserService`
  - 第三方账号绑定与查询

**OAuth2 相关（Auth Server 存储 SPI，仅保留三类）**

- `IRegisteredClientService`
  - RegisteredClient 的加载与保存
- `IOAuth2AuthorizationService`
  - 授权码、token、refresh token 的存储/查询/撤销
- `IOAuth2AuthorizationConsentService`
  - 用户授权同意记录

> 以上接口仅声明，不落 DB 实现；由业务系统完成落地。

## 10. Auth Server 关键实现（设计级）

- FilterChain 切分：
  - Authorization Server 链（OIDC/OAuth2 端点）
  - Web 登录链（表单/短信/社交）
- Token 定制：`OAuth2TokenCustomizer<JwtEncodingContext>`
  - 注入 `tenant_id`、`roles`、`client_id`
- 多租户 issuer：
  - `SecurityIssuerResolver` 或 TenantResolver
  - 动态选择 AuthorizationServerSettings/JWKSource

## 11. Resource Server 接入规范

- 使用 `spring-boot-starter-oauth2-resource-server`
- 只接受 Bearer Token，无 Session
- 多租户：AuthenticationManagerResolver + JwtDecoder 动态切换

## 12. 管理端能力

- 租户管理
- OAuth2 客户端管理
- 用户与身份绑定
- Token 吊销与审计

## 13. 配置约定（示例）

```yaml
goya:
  security:
    authentication:
      login:
        allow-password-login: true
        allow-sms-login: true
    resource:
      jwt:
        issuer-uri: https://auth.example.com
      token-blacklist:
        enabled: true
```

## 14. 落地里程碑

- M1：Auth Server 基础端点 + JWT + Refresh Token
- M2：多租户 issuer / JWK / client 隔离
- M3：短信登录 + 社交登录 + SSO
- M4：风控、审计、DPoP 等增强能力

---

如需定稿，我可以把下一步开发拆为具体模块与接口清单，并给出每个 SPI 的方法签名与调用时序。
