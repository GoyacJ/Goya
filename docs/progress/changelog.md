# Changelog

## [Unreleased]

### Added
- 初始化 Codex 项目规范文件：`AGENTS.md`。
- 新增项目专属 Codex skills（开发流程、依赖治理、文档治理）。
- 建立完整文档体系（入门、架构、开发、运维、进度）。
- 新增 `goya-thread-worktree` skill，支持一线程一 worktree（Desktop 优先，CLI 兜底）。
- 新增并行 thread 的 worktree SOP 与初始化/回收脚本。
- 完成 `component-security` 企业版实现：`security-core`、`security-authentication`、`security-oauth2`、`security-authorization`。
- 新增统一认证 API（密码/短信/社交/小程序/MFA）与最小登录页 `GET /security/login`。
- 新增 OAuth2.1 扩展授权类型 `urn:goya:grant-type:pre-auth-code`。
- 新增安全模块治理错误码：`SecurityErrorCode`、`SecurityErrorCodeCatalog`。
- 新增资源侧一致性校验过滤器：`HeaderClaimConsistencyFilter`（默认 `STRICT`）。
- 新增 OAuth2 JDBC 密钥轮换实现：`oauth2_jwk`、`JdbcOAuth2JwkManager`（`P30D` 轮换 + `P7D` 重叠）。
- 新增安全设计文档：`docs/architecture/component-security-design.md`。
- 新增安全部署文档：`docs/operations/security-deploy.md`。

### Changed
- README 与文档入口迁移到 Codex 文档体系。
- 开发流程默认切换为 Codex。
- 项目 skill 目录由旧目录迁移为 `.agents/skills`。
- `AGENTS.md` 与 `docs/development/codex-workflow.md` 强制要求新 thread 先初始化独立 worktree。
- `SecurityCoreProperties` 扩展多租户与 claim 配置并保持兼容。
- 资源服务支持 `AUTO/JWT/OPAQUE` 混合令牌校验与租户一致性校验。
- 认证流程切换为 `AuthenticationManager + AuthenticationProvider` 链实现。
- API 鉴权键从原始 URI 切换为 `servlet-scan mappingCode`，API action 固定为 `ACCESS`。
- `WebAccessContextResolver` 与 `WebTenantResolver` 改为优先消费统一认证上下文，仅未认证入口允许 header 回退。
- `pre_auth_code` 与 `mfa_challenge` 改为原子一次性消费，阻断并发重放。
- `security-authentication` / `security-oauth2` / `security-authorization` 从占位模块变更为可用模块。

### Removed
- 移除模块中的测试依赖（JUnit）。
- 清理残留测试目录（若存在）。
