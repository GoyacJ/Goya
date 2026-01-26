# Goya 安全模块开发辅助

## 描述
辅助开发 Goya 安全模块相关功能，包括认证、授权、OAuth2、JWT 等。

## 使用场景
- 添加新的登录方式
- 配置 OAuth2 Client
- 创建权限验证逻辑
- 生成 JWT Token
- 配置多租户

## 功能清单

### 1. 添加登录方式

示例："添加邮箱验证码登录"

生成：
- `EmailAuthenticationToken.java`
- `EmailAuthenticationProvider.java`
- `EmailAuthenticationConverter.java`
- 配置类更新

### 2. OAuth2 Client 配置

示例："创建一个 Web 应用的 OAuth2 Client"

生成：
```java
RegisteredClient.withId(UUID.randomUUID().toString())
    .clientId("web-client")
    .clientSecret("{bcrypt}...")
    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
    .redirectUri("http://localhost:3000/callback")
    .scope("read")
    .scope("write")
    .build();
```

### 3. 权限验证

示例："添加用户管理权限验证"

生成：
```java
@PreAuthorize("hasAuthority('user:write')")
public void updateUser(User user) {
    // 实现
}
```

### 4. JWT 生成和验证

示例："生成包含租户信息的 JWT"

生成 Token 生成和验证代码

### 5. 多租户配置

示例："配置基于 Path 的租户识别"

生成租户解析器和拦截器

## 安全检查清单

在生成代码后，自动检查：
- [ ] 密码是否使用 BCrypt 加密
- [ ] Token 是否有过期时间
- [ ] 是否记录安全审计日志
- [ ] 敏感信息是否加密
- [ ] 是否有权限验证

## 最佳实践

- 密码强度 >= 10
- Access Token <= 30 分钟
- 使用 RS256 签名
- 记录所有安全事件
- 敏感信息不放 JWT

## 参考资料

- OAuth2.1 规范
- Spring Security 文档
- Goya 安全规范
