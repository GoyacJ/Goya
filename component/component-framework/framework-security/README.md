# framework-security

`framework-security` 是 Goya 的策略授权内核模块，提供 SRA（Subject / Resource / Action）模型、策略评估管线与扩展 SPI。

## 核心能力

- 统一授权请求模型：`AuthorizeRequest`
- 主体/资源上下文解析：`SubjectResolver`、`ResourceResolver`
- 策略存储扩展：`PolicyRepository`
- 策略引擎：`PolicyEngine`
- 决策合并：`DefaultDecisionEvaluator`（拒绝优先、默认拒绝）
- 变更事件支持：策略变更可通过总线实现运行时生效

## 关键设计

- 默认行为为 `Deny by default`，无有效策略时返回拒绝。
- 模块只负责授权判定，不负责登录态、token 生命周期。
- 可被 `security-authorization`（API 权限）与 `component-mybatisplus`（数据权限）同时复用。

## 与安全闭环的关系

- 资源侧由 `PolicyAuthorizationFilter` 调用本模块授权服务实现动态 API 权限。
- 数据侧由 `GoyaDataPermissionHandler` 复用本模块策略模型执行行级过滤。

## 典型扩展点

- 自定义 `SubjectResolver`
- 自定义 `ResourceResolver`
- 自定义 `PolicyRepository`
- 自定义 `PolicyEngine`

## 构建校验

```bash
mvn -pl component/component-framework/framework-security -am -DskipTests validate
```
