# 依赖治理规则

## 禁止项
- 禁止依赖聚合 POM 模块（`packaging=pom`）。
- 禁止跨层反向依赖（基础层反向依赖应用层）。

## 推荐项
- 业务模块默认依赖 `framework-boot-starter` 获取统一能力。
- 子模块版本统一由父 POM 与 BOM 管理，不在子模块硬编码版本。

## 自动配置规则
新增 `@AutoConfiguration` 时，必须同步登记到：
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 验证命令
```bash
mvn -pl <module> -am -DskipTests validate
```
