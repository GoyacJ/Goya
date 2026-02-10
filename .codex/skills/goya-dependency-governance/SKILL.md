---
name: goya-dependency-governance
description: 处理 Maven 依赖、模块边界和自动配置注册时使用，避免聚合依赖和自动配置失效。
---

# Goya Dependency Governance

## 目标
- 保障 Maven 依赖可解析、模块边界清晰、自动配置可生效。

## 规则
1. 业务模块只能依赖可发布 Jar 模块，不能依赖聚合 POM。
2. 默认通过 `framework-boot-starter` 暴露框架能力。
3. 任何 `@AutoConfiguration` 类必须在 imports 文件中显式注册。
4. 依赖变更后至少做一次目标模块 `validate`（`-DskipTests`）。

## 重点检查
- `packaging` 是否为 `pom`。
- 依赖是否由父 POM 统一管理。
- 是否出现跨层级直接依赖（如 platform 反向依赖上层聚合）。

## 参考
- `references/module-dependency-rules.md`
