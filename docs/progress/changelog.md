# 更新日志 | Changelog

## [Unreleased]

### Added
- 多租户 Issuer 支持
- 短信验证码登录
- 社交登录框架

### Changed
- 优化 JWT Token 生成性能
- 改进缓存同步机制

### Fixed
- 修复多线程下的租户上下文问题

## [0.3.0] - 2025-01-24

### Added
- OAuth2.1 授权服务器
- Authorization Code + PKCE 流程
- JWT Access Token 签发
- Refresh Token 管理
- Token 撤销功能
- OIDC Discovery 端点

### Changed
- 重构安全模块结构
- 优化 SecurityUser 构建器

## [0.2.0] - 2025-01-20

### Added
- Spring Security 7 集成
- 用户名密码认证
- SecurityUser 领域模型
- IUserService SPI 接口

### Changed
- 统一异常处理

## [0.1.0] - 2025-01-10

### Added
- 项目初始化
- BOM 依赖管理
- component-core 模块
- component-framework 模块
- 自动配置机制

---

版本格式遵循 [Semantic Versioning](https://semver.org/)。
