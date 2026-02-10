# Codex 开发工作流

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
