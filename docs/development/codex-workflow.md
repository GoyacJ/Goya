# Codex 开发工作流

## 0. 新 thread 与 worktree
1. 每个新 thread 必须绑定独立 worktree，避免并行改动互相干扰。
2. 优先使用 Codex Desktop 的 Worktree 创建能力。
3. CLI 兜底命令：
   - 初始化：`.agents/skills/goya-thread-worktree/scripts/init_thread_worktree.sh --topic <topic>`
   - 回收：`.agents/skills/goya-thread-worktree/scripts/cleanup_thread_worktree.sh --thread-id <thread-id> --topic <topic>`
4. 分支命名规范：`codex/<thread-short>-<topic>`。
5. worktree 根目录：`/Users/goya/.codex/worktrees/goya/`。

## 1. 开发前
1. 阅读 `docs/SUMMARY.md`。
2. 明确变更模块与影响边界。
3. 使用 `rg` 检索现有实现，优先复用。

## 2. 开发中
1. 仅修改必要文件。
2. 遵守依赖治理规则。
3. 敏感配置使用环境变量占位。

## 3. 开发后
1. 执行构建验证（跳过测试）：
   - 模块：`mvn -pl <module> -am -DskipTests validate`
   - 全仓：`mvn -DskipTests compile`
2. 更新文档：至少更新 `docs/progress/changelog.md`。
3. 若结构变化，更新 `docs/architecture/module-map.md`。
