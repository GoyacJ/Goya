# 统一认证授权框架

基于 Spring Authorization Server 和 OAuth2.1 的企业级统一认证授权框架实现。

## 功能特性

### 1. 多种登录方式支持（Authorization Code 流程）

#### 用户名密码登录（带验证码）
- **流程**: Authorization Code + PKCE（OAuth2.1标准）
- **端点**: `POST /login`（登录页面）→ `GET /oauth2/authorize`（授权端点）
- **参数**:
  - `grant_type=password`
  - `username`: 用户名
  - `password`: 密码
  - `captcha`: 验证码值（可选）
  - `captcha_key`: 验证码标识（可选）

#### 短信验证码登录
- **流程**: Authorization Code + PKCE（OAuth2.1标准）
- **端点**: `POST /login`（登录页面）→ `GET /oauth2/authorize`（授权端点）
- **参数**:
  - `grant_type=sms`
  - `phone`: 手机号
  - `sms_code`: 短信验证码

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
- ✅ **DPoP支持**: 支持DPoP-bound Access Tokens（RFC 9449）
- ✅ **标准流程**: 完全符合OAuth2.1规范，使用Authorization Code流程

## 核心组件

### 多模式登录认证（Multi-Mode Authentication）

#### 密码登录
- `PasswordAuthenticationConverter`: 从请求中提取密码登录参数
- `PasswordAuthenticationToken`: 封装密码认证凭证
- `PasswordAuthenticationProvider`: 验证用户凭证，返回UsernamePasswordAuthenticationToken

#### 短信登录
- `SmsAuthenticationConverter`: 从请求中提取短信登录参数
- `SmsAuthenticationToken`: 封装短信认证凭证
- `SmsAuthenticationProvider`: 验证短信验证码，查找用户，返回UsernamePasswordAuthenticationToken

#### 社交登录
- `SocialAuthenticationConverter`: 从请求中提取社交登录参数
- `SocialAuthenticationToken`: 封装社交认证凭证
- `SocialAuthenticationProvider`: 查找或创建用户，返回UsernamePasswordAuthenticationToken

#### 统一登录过滤器
- `UnifiedLoginAuthenticationFilter`: 统一处理多种登录方式的过滤器
- `CompositeAuthenticationConverter`: 组合多个Converter，按顺序尝试

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

### Authorization Code 流程（标准OAuth2.1）

#### 第一步：获取授权码

```bash
# 1. 用户访问受保护资源，重定向到授权端点
GET http://localhost:8080/oauth2/authorize?client_id=client-id&response_type=code&redirect_uri=http://client.example.com/callback&scope=read write&code_challenge=xxx&code_challenge_method=S256

# 2. 如果未登录，重定向到登录页面
GET http://localhost:8080/login

# 3. 用户提交登录（密码登录）
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded

grant_type=password
&username=user@example.com
&password=password123
&captcha=1234
&captcha_key=uuid-key

# 4. 登录成功后，自动重定向回授权端点，生成授权码
GET http://localhost:8080/oauth2/authorize?client_id=client-id&response_type=code&redirect_uri=http://client.example.com/callback&scope=read write&code_challenge=xxx&code_challenge_method=S256
# 返回: redirect_uri?code=authorization_code&state=xxx
```

#### 第二步：交换Token

```bash
# 使用授权码换取Token
POST http://localhost:8080/oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code=authorization_code
&redirect_uri=http://client.example.com/callback
&client_id=client-id
&code_verifier=code_verifier

# 响应
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",  // 或 "DPoP"（如果使用DPoP）
  "expires_in": 3600,
  "refresh_token": "opaque_refresh_token",
  "scope": "read write"
}
```

### 短信登录示例

```bash
# 在登录页面提交短信登录
POST http://localhost:8080/login
Content-Type: application/x-www-form-urlencoded

grant_type=sms
&phone=13800138000
&sms_code=123456
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

1. **OAuth2.1标准流程**: 完全符合OAuth2.1规范，使用Authorization Code + PKCE流程
2. **PKCE强制要求**: 所有Authorization Code流程必须使用PKCE，符合OAuth2.1规范
3. **Token安全**: 
   - Access Token建议设置15分钟过期时间（JWT格式）
   - Refresh Token存储在Redis，支持撤销和轮转（Opaque格式）
   - 支持DPoP-bound Access Tokens，防止Token泄露
4. **验证码**: 建议所有登录方式都启用验证码，防止暴力破解
5. **HTTPS**: 生产环境必须使用HTTPS
6. **无状态设计**: 完全无状态，支持水平扩展

## 扩展点

### 添加新的登录方式

如需添加新的登录方式（如人脸识别、指纹等），参考以下步骤：

1. 创建`XxxAuthenticationConverter`继承`AbstractAuthenticationConverter`
2. 创建`XxxAuthenticationToken`实现`Authentication`接口
3. 创建`XxxAuthenticationProvider`实现`AuthenticationProvider`接口
4. 在`AuthorizationAutoConfiguration`中注册Converter和Provider
5. 将新的Converter添加到`CompositeAuthenticationConverter`中

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

