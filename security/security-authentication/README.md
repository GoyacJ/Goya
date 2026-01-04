# 统一认证授权框架

基于 Spring Authorization Server 和 OAuth2.1 的企业级统一认证授权框架实现。

## 功能特性

### 1. 多种登录方式支持

#### 用户名密码登录（带验证码）
- **Grant Type**: `password`（企业内部分机扩展）
- **端点**: `POST /oauth2/token`
- **参数**:
  - `grant_type=password`
  - `username`: 用户名
  - `password`: 密码
  - `captcha`: 验证码值（可选但建议必填）
  - `captcha_key`: 验证码标识（可选但建议必填）
  - `client_id`: 客户端ID
  - `scope`: 授权范围（可选）

#### 短信验证码登录
- **Grant Type**: `sms`（企业内部分机扩展）
- **端点**: `POST /oauth2/token`
- **参数**:
  - `grant_type=sms`
  - `phone`: 手机号
  - `sms_code`: 短信验证码
  - `client_id`: 客户端ID
  - `scope`: 授权范围（可选）

#### 第三方登录（微信、Gitee、GitHub）
- **流程**: OAuth2 Client Flow
- **端点**: `/login/oauth2/code/{registrationId}`
- **支持提供商**:
  - 微信（WeChat）
  - Gitee
  - GitHub

#### 单点登录（SSO）
- **流程**: Authorization Code + PKCE（OAuth2.1要求）
- **端点**: `GET /oauth2/authorize`
- **特性**: 强制PKCE，符合OAuth2.1规范

### 2. Token管理

#### Token格式
- **Access Token**: JWT格式，包含用户信息、权限等
- **Refresh Token**: Opaque格式，存储在Redis中

#### Token端点
- **Token Introspection**: `POST /oauth2/token/introspect` (RFC 7662)
- **Token Revocation**: `POST /oauth2/token/revoke` (RFC 7009)
- **Token黑名单**: 支持Token撤销和黑名单管理

### 3. OAuth2.1规范支持

- ✅ **PKCE强制要求**: 所有Authorization Code流程强制使用PKCE
- ✅ **Token Introspection**: 支持Token内省端点
- ✅ **Token Revocation**: 支持Token撤销端点
- ✅ **OIDC支持**: 支持OpenID Connect 1.0
- ⚠️ **自定义Grant Type**: Password和SMS Grant是内部分机扩展，不符合OAuth2.1标准

## 核心组件

### 自定义Grant Type

#### Password Grant Type
- `PasswordGrantAuthenticationConverter`: 从请求中提取认证参数
- `PasswordGrantAuthenticationToken`: 封装认证凭证
- `PasswordGrantAuthenticationProvider`: 验证验证码和用户凭证，生成Token

#### SMS Grant Type
- `SmsGrantAuthenticationConverter`: 从请求中提取手机号和验证码
- `SmsGrantAuthenticationToken`: 封装短信认证凭证
- `SmsGrantAuthenticationProvider`: 验证短信验证码，查找用户，生成Token

### OAuth2 Client配置

#### OAuth2ClientConfiguration
- 配置微信、Gitee、GitHub的OAuth2 Client注册
- 支持通过配置文件动态配置

#### SocialOAuth2UserService
- 处理第三方登录回调
- 支持用户绑定和自动注册（需实现）

### PKCE强制要求

#### PkceEnforcingRegisteredClientRepository
- 包装RegisteredClientRepository
- 自动为Authorization Code流程的客户端添加PKCE要求
- 对公开客户端强制要求PKCE

## 配置说明

### application.yml配置示例

```yaml
platform:
  security:
    authentication:
      # Token黑名单配置
      tokenBlackListConfig:
        tokenBlackListExpire: PT5M
        defaultReason: "Token is blacklisted"
      
      # JWT配置
      jwk:
        certificate: CUSTOM
        jksKeyStore: classpath*:certificate/ysmjjsy.jks
        jksKeyPassword: ysmjjsy
        jksStorePassword: ysmjjsy
        jksKeyAlias: ysmjjsy
      
      # 密码Grant Type配置
      passwordGrantConfig:
        enableCaptcha: true
        captchaCategory: SPEC
        minPasswordLength: 6
        maxPasswordLength: 20
      
      # 短信Grant Type配置
      smsGrantConfig:
        codeExpireSeconds: 300
        codeLength: 6
        cacheName: sms:verification:code
        allowAutoCreateUser: false
      
      # OAuth2客户端配置（第三方登录）
      oauth2ClientConfig:
        enabled: true
        wechat:
          enabled: true
          clientId: your-wechat-appid
          clientSecret: your-wechat-secret
          scope: snsapi_login
          redirectUri: http://localhost:8080/login/oauth2/code/wechat
        gitee:
          enabled: true
          clientId: your-gitee-client-id
          clientSecret: your-gitee-client-secret
          scope: user_info
          redirectUri: http://localhost:8080/login/oauth2/code/gitee
        github:
          enabled: true
          clientId: your-github-client-id
          clientSecret: your-github-client-secret
          scope: read:user
          redirectUri: http://localhost:8080/login/oauth2/code/github
      
      # SSO配置
      ssoConfig:
        requirePkce: true
        allowPublicClients: true
        authorizationCodeExpireSeconds: 300
```

## API使用示例

### 用户名密码登录

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic base64(client_id:client_secret)" \
  -d "grant_type=password" \
  -d "username=user@example.com" \
  -d "password=password123" \
  -d "captcha=1234" \
  -d "captcha_key=uuid-key" \
  -d "scope=read write"
```

### 短信验证码登录

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic base64(client_id:client_secret)" \
  -d "grant_type=sms" \
  -d "phone=13800138000" \
  -d "sms_code=123456" \
  -d "scope=read write"
```

### Token内省

```bash
curl -X POST http://localhost:8080/oauth2/token/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic base64(client_id:client_secret)" \
  -d "token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Token撤销

```bash
curl -X POST http://localhost:8080/oauth2/token/revoke \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Authorization: Basic base64(client_id:client_secret)" \
  -d "token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -d "token_type_hint=access_token"
```

## 安全注意事项

1. **自定义Grant Type**: Password和SMS Grant是OAuth2.1的非标准扩展，仅用于企业内部系统，不应对外暴露
2. **PKCE强制要求**: 所有Authorization Code流程必须使用PKCE，符合OAuth2.1规范
3. **Token安全**: 
   - Access Token建议设置15分钟过期时间
   - Refresh Token存储在Redis，支持撤销
4. **验证码**: 建议所有登录方式都启用验证码，防止暴力破解
5. **HTTPS**: 生产环境必须使用HTTPS

## 扩展点

### 自定义Grant Type

如需添加新的Grant Type，参考以下步骤：

1. 创建`XxxGrantAuthenticationConverter`实现`AuthenticationConverter`
2. 创建`XxxGrantAuthenticationToken`继承`OAuth2AuthorizationGrantAuthenticationToken`
3. 创建`XxxGrantAuthenticationProvider`实现`AuthenticationProvider`
4. 在`AuthorizationAutoConfiguration`中注册Converter和Provider

### 自定义用户服务

实现`ISecurityUserService`接口，提供用户查找和验证功能。

## 参考文档

- [Spring Authorization Server Documentation](https://github.com/spring-projects/spring-authorization-server)
- [OAuth 2.1 Specification (RFC 9120)](https://www.rfc-editor.org/rfc/rfc9120.html)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)

## 作者

goya

## 许可证

Apache License 2.0

