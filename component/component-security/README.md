# component-security

`component-security` 是 Goya 的企业级安全组件聚合模块，覆盖认证、授权、OAuth2.1、SSO 与会话治理。

## 子模块

- `security-core`：安全核心模型、SPI、上下文与会话生命周期抽象
- `security-authentication`：统一认证入口（密码/短信/社交/小程序/MFA）与会话命令 API
- `security-oauth2`：OAuth2.1 授权服务器、扩展授权、双 token、撤销索引
- `security-authorization`：资源服务器校验、一致性校验、吊销拦截、策略联动

## 闭环能力

- 统一认证授权：支持 PC / 移动端 / 小程序
- SSO：登录桥接 + OIDC 登出联动
- OAuth2.1：禁隐式、支持 PKCE、支持扩展 `pre-auth-code`
- 双 Token：`AccessToken=JWT`、`RefreshToken=Opaque`（默认轮换）
- 动态权限：策略引擎实时判定 API 权限
- 数据权限联动：与 `component-mybatisplus` 协同
- 会话治理：`logout/kickout/revokeByUser` 全链路生效

## 关键文档

- [安全架构设计](../../docs/architecture/component-security-design.md)
- [安全部署指南](../../docs/operations/security-deploy.md)

## 构建校验

```bash
mvn -pl component/component-security -am -DskipTests validate
```
