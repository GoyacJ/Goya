---
name: goya-codex-workflow
description: 在 Goya 仓库进行任何代码、配置或构建改动时使用。提供 Codex 标准执行流程、验证命令和无测试策略。
---

# Goya Codex Workflow

## 何时使用
- 任何涉及 `pom.xml`、`src/main/**`、`deploy/**`、`docs/**` 的改动。

## 执行步骤
1. 先读 `docs/SUMMARY.md`，确认需要更新的文档位置。
2. 锁定变更模块并最小化改动范围。
3. 按模块验证：`mvn -pl <module> -am -DskipTests validate`。
4. 若影响全局依赖或聚合结构，执行：`mvn -DskipTests compile`。
5. 更新 `docs/progress/changelog.md`。

## 必须遵守
- 禁止新增 `src/test/**`。
- 禁止新增测试依赖。
- 不执行和不要求 `mvn test` 作为验收门槛。

## 参考
- `references/commands.md`
