# security-core

`security-core` 提供安全域的核心模型、标准常量、SPI 接口与上下文能力。

## 核心内容

- 安全用户模型：`SecurityUser`
- 常量与 Claim 约定：`StandardClaimNamesConst`
- 错误码体系：`SecurityErrorCode`
- SPI：
  - `IUserService`
  - `ISocialUserService`
  - `ITenantService`
  - `IOtpService`
  - `IRolePermissionService`
- 安全会话生命周期抽象：`SecuritySessionLifecycleService`
- 会话撤销结果：`SecuritySessionRevocationResult`

## 设计原则

- 只定义安全域契约与公共模型，不实现业务账号逻辑。
- 业务系统通过 SPI 注入用户、租户、OTP、社交绑定能力。

## 构建校验

```bash
mvn -pl component/component-security/security-core -am -DskipTests validate
```
