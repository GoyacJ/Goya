# AGENTS.md

## 适用范围
- 本文件适用于仓库根目录及全部子目录。
- 本项目后续默认通过 Codex 进行开发与维护。

## 强制工作流
0. 每次通过 Codex 开启新 thread 时，必须先创建独立 worktree（优先使用 Codex Desktop Worktree；CLI 兜底：`.agents/skills/goya-thread-worktree/scripts/init_thread_worktree.sh --topic <topic>`）。
1. 开发前先阅读：`/Users/goya/Repo/Git/Goya/Goya/docs/SUMMARY.md`。
2. 仅在目标模块内改动，避免跨模块无关修改。
3. 变更后至少执行一次 Maven 构建校验（跳过测试）：`mvn -DskipTests compile` 或按模块 `mvn -pl <module> -am -DskipTests validate`。
4. 任何功能变更都必须同步更新文档（至少更新 `docs/progress/changelog.md`）。

## 项目约束
- 不新增测试文件（`src/test/**`）。
- 不新增测试依赖（如 JUnit、Mockito、Testcontainers）。
- 不要求执行 `mvn test`。
- 变更以“可构建、可启动、文档一致”为目标。

## 依赖治理
- Maven 子模块禁止依赖聚合 POM（`packaging=pom` 的聚合模块）。
- 优先依赖可发布模块；框架入口默认使用 `framework-boot-starter`。
- 新增 `@AutoConfiguration` 时，必须同步更新
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

## 安全与配置
- 禁止提交明文密钥、密码、Token。
- 配置文件中的敏感字段必须使用环境变量占位。
- 仅提交模板配置（例如 `.env.example`）时，值必须是占位符。

## 文档位置
- 文档总索引：`/Users/goya/Repo/Git/Goya/Goya/docs/SUMMARY.md`
- 项目专属 skills：`/Users/goya/Repo/Git/Goya/Goya/.agents/skills/`
