# 更新日志 | Changelog

## [Unreleased]

### Added
- 多租户 Issuer 支持
- 短信验证码登录
- 社交登录框架
- SRA 策略模型与数据权限执行（JSON DSL、行/列级约束）
- 权限变更事件发布链路（framework-bus）
- 多租户混合模式（共享库 + 独立库，动态数据源）

### Changed
- 优化 JWT Token 生成性能
- 改进缓存同步机制

### Fixed
- 修复多线程下的租户上下文问题

## [0.4.0] - 2025-01-29

### Added - 项目重构
- **模块重组**：将原 component 模块重组为更清晰的结构
- **Framework 聚合模块**：新增 `component-framework` 聚合模块
  - `framework-core`：核心工具类、基础定义
  - `framework-common`：公共组件、工具类
  - `framework-masker`：数据脱敏功能
  - `framework-crypto`：加密解密工具
  - `framework-cache`：缓存抽象层
  - `framework-bus`：消息总线抽象
  - `framework-log`：日志增强
  - `framework-oss`：对象存储抽象
  - `framework-servlet`：Servlet 增强
  - `framework-boot-starter`：自动配置启动器
- **Redis 模块增强**：新增 `component-redis`
  - 分布式锁（可重入锁/公平锁/读写锁）
  - 布隆过滤器
  - 延迟队列 / 可靠延迟队列
  - 分布式限流器
  - Topic 消息发布订阅
  - 原子操作服务
- **消息总线拆分**：
  - 新增 `component-kafka`：Kafka 消息总线实现
  - 新增 `component-rabbitmq`：RabbitMQ 消息总线实现
- **OSS 模块拆分**：
  - 新增 `component-oss-aliyun`：阿里云 OSS 完整实现
  - 新增 `component-oss-s3`：AWS S3 实现
  - 新增 `component-oss-minio`：MinIO 实现
- **项目结构调整**：
  - 前端项目移至 `goya-web-ui/` 目录
  - 部署配置移至 `deploy/` 目录
  - 文档移至 `docs/` 目录

### Changed
- 重构数据库模块为 `component-mybatisplus`
- 优化安全模块结构
- 更新所有模块依赖版本

### Removed
- 移除原 `component-core`（合并到 framework-core）
- 移除原 `component-web`（合并到 framework-servlet）
- 移除原 `component-cache`（拆分为 framework-cache + component-redis）
- 移除原 `component-bus`（拆分为 framework-bus + component-kafka/rabbitmq）
- 移除原 `component-database`（重构为 component-mybatisplus）
- 移除原 `component-oss`（拆分为独立 OSS 实现模块）

## [0.3.0] - 2025-01-24

### Added
- OAuth2.1 授权服务器
- Authorization Code + PKCE 流程
- JWT Access Token 签发
- Refresh Token 管理
- Token 撤销功能
- OIDC Discovery 端点
- 项目文档体系
- Cursor Rules 开发规范
- Cursor Skills 开发工具

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
