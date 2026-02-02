# 认证授权系统需求 | Authentication & Authorization System Requirements

## 1. 功能概述

构建符合 OAuth2.1 和 OIDC 标准的企业级认证授权系统，支持多种登录方式、多租户、SSO 等能力。

## 2. 核心功能

### 2.1 OAuth2.1 授权服务器

#### 功能描述
实现符合 OAuth2.1 标准的授权服务器，支持标准的授权流程和端点。

#### 支持的授权类型
- ✅ Authorization Code + PKCE
- ✅ Refresh Token
- ✅ Client Credentials
- ❌ Resource Owner Password（不推荐，不支持）
- ❌ Implicit（已废弃，不支持）

#### 标准端点
- `GET /.well-known/openid-configuration` - OIDC Discovery
- `GET /oauth2/authorize` - 授权端点
- `POST /oauth2/token` - Token 端点
- `GET /oauth2/jwks` - JWK Set 端点
- `GET /userinfo` - 用户信息端点
- `POST /oauth2/revoke` - Token 撤销端点
- `POST /oauth2/introspect` - Token 内省端点

### 2.2 多登录方式

#### 2.2.1 用户名密码登录

**流程**：
```
1. 用户输入用户名和密码
2. 验证验证码（可选）
3. 验证用户名和密码
4. 验证通过，创建 Session
5. 重定向到授权页面
```

**安全要求**：
- 密码使用 BCrypt 加密（强度 10+）
- 登录失败限制（5次/5分钟）
- 支持账号锁定策略

#### 2.2.2 短信验证码登录

**流程**：
```
1. 用户输入手机号
2. 发送短信验证码
3. 用户输入验证码
4. 验证码校验通过
5. 创建 Session，重定向
```

**实现要求**：
- 验证码有效期：5 分钟
- 验证码格式：6 位数字
- 验证码存储：Redis
- 发送频率限制：1次/分钟，5次/小时

#### 2.2.3 社交登录

**支持平台**：
- 微信小程序
- 微信公众号
- GitHub / Google（基于 JustAuth）

**流程**：
```
1. 用户点击社交登录按钮
2. 跳转到第三方授权页面
3. 用户授权后回调
4. 获取第三方用户信息
5. 绑定或创建系统用户
6. 创建 Session，重定向
```

### 2.3 Token 管理

#### 2.3.1 Access Token (JWT)

**规格**：
- **类型**：JWT
- **签名算法**：RS256
- **有效期**：15 分钟（可配置 5-30 分钟）
- **Claims**：
  ```json
  {
    "iss": "https://auth.example.com/t/tenant-001",
    "sub": "user-123",
    "aud": ["api.example.com"],
    "exp": 1737702000,
    "iat": 1737701100,
    "jti": "token-uuid",
    "tenant_id": "tenant-001",
    "client_id": "client-001",
    "scope": "read write",
    "roles": ["ADMIN", "USER"]
  }
  ```

#### 2.3.2 Refresh Token (Opaque)

**规格**：
- **类型**：Opaque（不透明）
- **格式**：UUID
- **有效期**：7-30 天（可配置）
- **存储**：数据库（哈希值）
- **Rotation**：刷新时自动轮换

#### 2.3.3 Token 撤销

**支持场景**：
- 用户主动登出
- 管理员强制下线
- Token 泄露应急

**实现方式**：
- **黑名单**：Redis 存储 JTI
- **Refresh Token**：数据库标记失效

### 2.4 多租户

#### 2.4.1 租户识别

**识别方式**（优先级从高到低）：
1. **Path 路径**：`/t/{tenant}/.well-known/openid-configuration`
2. **Subdomain 子域名**：`tenant-001.example.com`
3. **Header 请求头**：`X-Tenant-ID: tenant-001`

#### 2.4.2 租户隔离

**隔离维度**：
- **Issuer**：每个租户独立的 Issuer
- **JWK**：每个租户独立的签名密钥
- **Client**：客户端数据隔离
- **Authorization**：授权数据隔离
- **User**：用户数据隔离（可选）

#### 2.4.3 租户配置

**可配置项**：
- 是否启用密码登录
- 是否启用短信登录
- 是否启用社交登录
- Token 有效期
- 登录页定制（Logo / 主题色）

### 2.5 SSO 单点登录

#### 功能描述
用户在一个应用登录后，访问其他应用无需再次登录。

#### 实现方案
- **Session**：认证服务器维护登录 Session
- **存储**：Spring Session + Redis
- **流程**：
  ```
  1. 用户访问 App A
  2. 未登录，跳转到认证服务器
  3. 认证服务器检查 Session
  4. 已有 Session，直接授权
  5. 返回 App A，自动登录
  ```

### 2.6 权限模型

#### RBAC 模型

```
User (用户)
  └─ has many ─> Role (角色)
       └─ has many ─> Permission (权限)
            └─ Resource (资源) + Action (操作)
```

**示例**：
- **用户**：张三
- **角色**：管理员、财务
- **权限**：
  - `user:read` - 查看用户
  - `user:write` - 编辑用户
  - `order:read` - 查看订单

#### OAuth2 Scope

- **API 级别权限**：`read`, `write`, `admin`
- **资源级别权限**：`user.read`, `order.write`
- **Scope 验证**：在资源服务器端验证

#### SRA 策略模型（数据权限）

在 RBAC 的基础上，引入 Subject / Resource / Action 策略模型用于数据权限执行：
- **Subject**：用户/角色/团队/组织
- **Resource**：表/字段/API
- **Action**：QUERY/CREATE/UPDATE/DELETE
- **Policy**：ALLOW/DENY + Scope（RESOURCE/ROW/COLUMN）
- **Row Filter**：使用 JSON DSL 描述行级条件
- **Column Constraint**：允许/拒绝字段列表

## 3. 非功能需求

### 3.1 性能

| 指标 | 要求 |
|------|------|
| 登录 API | P99 < 500ms |
| Token 签发 | P99 < 100ms |
| Token 验证 | P99 < 50ms |
| 并发登录 | > 1000/s |

### 3.2 安全

- **传输加密**：强制 HTTPS
- **密码安全**：BCrypt + Salt
- **Token 安全**：短有效期 + Rotation
- **防暴力破解**：登录限流 + 验证码
- **审计日志**：所有认证事件记录

### 3.3 可用性

- **服务可用性**：99.9%
- **故障恢复**：< 5 分钟
- **数据备份**：每日备份

## 4. 用户故事

### 4.1 用户登录

**作为**普通用户  
**我想要**使用用户名密码登录  
**以便于**访问系统

**验收标准**：
- 输入用户名和密码
- 验证码校验通过
- 登录成功，跳转到首页

### 4.2 SSO 体验

**作为**企业用户  
**我想要**登录一次，访问所有应用  
**以便于**提高工作效率

**验收标准**：
- 在 App A 登录
- 访问 App B 无需再次登录
- 登出 App A，App B 也自动登出

### 4.3 多租户隔离

**作为**租户管理员  
**我想要**我的租户数据完全隔离  
**以便于**保证数据安全

**验收标准**：
- 租户 A 无法访问租户 B 的数据
- 租户 A 无法使用租户 B 的 Token
- 租户 A 可以独立配置登录方式

## 5. API 设计

### 5.1 登录接口

```http
POST /login
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456&captchaKey=xxx&captchaCode=1234
```

**响应**：
```http
HTTP/1.1 302 Found
Location: /oauth2/authorize?...
Set-Cookie: SESSION=xxx; HttpOnly; Secure; SameSite=Lax
```

### 5.2 Token 接口

```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic Y2xpZW50LWlkOmNsaWVudC1zZWNyZXQ=

grant_type=authorization_code&code=xxx&redirect_uri=xxx&code_verifier=xxx
```

**响应**：
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "550e8400-e29b-41d4-a716-446655440000",
  "token_type": "Bearer",
  "expires_in": 900,
  "scope": "read write"
}
```

## 6. 数据模型

### 6.1 用户表

```sql
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(100) NOT NULL,
  mobile VARCHAR(20),
  email VARCHAR(100),
  enabled BOOLEAN DEFAULT TRUE,
  account_locked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 6.2 OAuth2 Client 表

```sql
CREATE TABLE oauth2_registered_client (
  id VARCHAR(100) PRIMARY KEY,
  client_id VARCHAR(100) UNIQUE NOT NULL,
  client_secret VARCHAR(200) NOT NULL,
  client_name VARCHAR(200) NOT NULL,
  authorization_grant_types VARCHAR(1000) NOT NULL,
  redirect_uris TEXT,
  scopes VARCHAR(1000),
  tenant_id VARCHAR(50)
);
```

## 7. 开发计划

### Phase 1（已完成）
- ✅ 用户名密码登录
- ✅ OAuth2.1 授权码流程
- ✅ JWT Token 签发

### Phase 2（进行中）
- 🚧 短信验证码登录
- 🚧 多租户 Issuer
- 🚧 SSO 优化

### Phase 3（规划中）
- 📅 社交登录完善
- 📅 MFA 多因素认证
- 📅 DPoP 支持

## 8. 参考资料

- [OAuth 2.1](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-v2-1-09)
- [OIDC Core](https://openid.net/specs/openid-connect-core-1_0.html)
- [Spring Authorization Server](https://spring.io/projects/spring-authorization-server)
