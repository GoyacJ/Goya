# security-authorization

`security-authorization` 负责资源侧令牌校验、策略授权联动与吊销拦截。

## 核心能力

- 资源服务器模式：`AUTO` / `JWT` / `OPAQUE`
- Header/Claim 一致性校验：`X-Tenant-Id`、`X-User-Id`
- 吊销拦截：`RevokedTokenFilter`
- 动态 API 权限：`PolicyAuthorizationFilter`
- `mappingCode` 资源键（`method + pathPattern`）

## 过滤器链（核心）

- `HeaderClaimConsistencyFilter`
- `RevokedTokenFilter`
- `PolicyAuthorizationFilter`

说明：
- 会话撤销后，旧 token 在资源侧立即返回 401。
- `PolicyAuthorizationFilter` 默认对内置安全端点
  `POST /api/security/auth/logout`、`POST /api/security/auth/kickout` 做策略豁免，避免无策略时阻断会话命令。

## 关键配置

配置前缀：`goya.security.resource`

- `mode`
- `consistency-mode`
- `permit-all-patterns`
- `tenant-header` / `user-header`
- `role-ids-claim` / `team-ids-claim` / `org-ids-claim`
- `revoked-cache-name`
- `policy-enabled`

## 构建校验

```bash
mvn -pl component/component-security/security-authorization -am -DskipTests validate
```
