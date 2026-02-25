# Goya Thread Worktree SOP

## 目标

- 每个 Codex thread 使用独立 worktree，互不干扰。
- 主工作区用于基线维护与集成，不承载并行 thread 的直接开发。

## 命名约定

- 分支：`codex/<thread-short>-<topic>`
- 目录：`/Users/goya/.codex/worktrees/goya/<thread-short>-<topic>`
- `thread-short`：`CODEX_THREAD_ID` 归一化后取前 12 位
- `topic`：小写短横线 slug

## 优先路径（Codex Desktop）

1. 新建 thread 时启用独立 Worktree。
2. 绑定分支名遵循 `codex/<thread-short>-<topic>`。
3. 进入该 worktree 后再开始代码改动。

## CLI 兜底路径

```bash
# 初始化
scripts/init_thread_worktree.sh --topic <topic>

# 回收
scripts/cleanup_thread_worktree.sh --thread-id <thread-id> --topic <topic>
```

## 幂等与安全要求

1. 同一 thread + topic 重复初始化，不重复创建 worktree。
2. 目标路径已存在但非 worktree，必须报错并停止。
3. 回收时禁止删除当前 shell 所在 worktree。
4. 回收后执行 `git worktree prune` 清理悬挂记录。

## 常见检查

```bash
git worktree list
git branch -vv
```
