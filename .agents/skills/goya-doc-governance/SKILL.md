---
name: goya-doc-governance
description: 在 Goya 仓库维护文档体系时使用，确保架构、开发、运维、进度文档与代码状态一致；仅代码实现且不涉及文档变更时不触发。
---

# Goya Doc Governance

## 适用场景
- 修改模块结构、构建方式、运行方式、配置或安全策略。

## 必做更新
1. 更新 `docs/progress/changelog.md`。
2. 若模块边界变化，更新 `docs/architecture/module-map.md`。
3. 若命令或流程变化，更新 `docs/development/codex-workflow.md` 或 `docs/operations/build-and-release.md`。

## 文档原则
- 只记录“当前分支真实可执行”内容。
- 避免未来式、占位式描述。
- 路径、命令、端口必须可在仓库中验证。

## 参考
- `references/doc-update-checklist.md`
