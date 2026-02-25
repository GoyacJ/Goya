---
name: goya-thread-worktree
description: 当任务涉及在 Codex 中开启新 thread、并行推进多个 thread、或需要隔离 Git 工作区时触发。该 skill 为每个 thread 绑定独立 git worktree（优先 Codex Desktop 内建 Worktree，CLI 脚本兜底），并提供初始化、幂等复用与回收流程；单线程一次性改动或只读分析时不触发。
---

# Goya Thread Worktree

将 Goya 的并行 thread 开发流程固化为“一线程一 worktree”，避免分支污染、未跟踪文件串扰和误提交。

## 执行原则

1. 每个 thread 必须使用独立 worktree 路径。
2. 分支命名固定为 `codex/<thread-short>-<topic>`。
3. worktree 目录固定在 `/Users/goya/.codex/worktrees/goya/<thread-short>-<topic>`。
4. 新 thread 场景优先使用 Codex Desktop 的 Worktree 创建能力。
5. CLI 或异常场景使用 `scripts/init_thread_worktree.sh` 和 `scripts/cleanup_thread_worktree.sh`。

## 标准流程

1. 识别 topic（简短、可读，例如 `security-core`、`oauth-login`）。
2. 优先在 Codex Desktop 新 thread 时创建独立 worktree。
3. 若未通过 Desktop 创建，执行：
   - `scripts/init_thread_worktree.sh --topic <topic>`
4. 进入新 worktree 后再进行代码修改与构建。
5. thread 结束后执行：
   - `scripts/cleanup_thread_worktree.sh --thread-id <thread-id> --topic <topic>`

## CLI 兜底命令

```bash
# 默认从当前分支切出，thread-id 默认取 CODEX_THREAD_ID
scripts/init_thread_worktree.sh --topic security-core

# 指定基线分支与 thread-id
scripts/init_thread_worktree.sh --topic oauth2-login --base springboot4.0 --thread-id 019c46a4-82e0-72f0-910d-e9e612ba221e

# 干跑，查看将执行的命令
scripts/init_thread_worktree.sh --topic security-core --dry-run
```

```bash
# 按 thread-id + topic 回收
scripts/cleanup_thread_worktree.sh --thread-id 019c46a4-82e0-72f0-910d-e9e612ba221e --topic security-core

# 干跑回收
scripts/cleanup_thread_worktree.sh --thread-id 019c46a4-82e0-72f0-910d-e9e612ba221e --topic security-core --dry-run
```

## 输出要求

- 输出 thread 与 worktree 的映射：
  - `thread_id`
  - `base_branch`
  - `branch`
  - `worktree_path`
- 脚本重复执行必须幂等：同一 thread + topic 不重复创建。
- 回收必须防误删：禁止删除当前 shell 正在使用的 worktree。

## 参考

- `references/worktree-sop.md`
