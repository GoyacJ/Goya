# security-authentication

`security-authentication` 提供统一认证入口、最小登录页与会话命令能力。

## 统一认证 API

- `POST /api/security/auth/password/login`
- `POST /api/security/auth/sms/send`
- `POST /api/security/auth/sms/login`
- `GET /api/security/auth/social/{source}/authorize`
- `GET /api/security/auth/social/{source}/callback`
- `POST /api/security/auth/wx-mini/login`
- `POST /api/security/auth/mfa/challenge`
- `POST /api/security/auth/mfa/verify`
- `POST /api/security/auth/logout`
- `POST /api/security/auth/kickout`

## 会话命令能力

通过 `SecuritySessionCommandService` 统一处理：

- `logout`：支持 `CURRENT_SESSION`、`ALL_SESSIONS`、`BY_CLIENT`
- `kickout`：管理员按 `tenantId/userId/clientId` 撤销目标会话

说明：
- 会话命令最终委托 `SecuritySessionLifecycleService`。
- `kickout` 默认要求 `ROLE_ADMIN` 或 `security:kickout` 权限。

## SSO 登录桥接

- 登录页：`GET /security/login`
- 会话桥接：`POST /security/login/session`

## 社交默认适配

若未提供自定义 `ISocialUserService`，自动装配 `DefaultSocialUserServiceAdapter`，复用 `component-social` 的 `SocialBindingStore`。

## 构建校验

```bash
mvn -pl component/component-security/security-authentication -am -DskipTests validate
```
