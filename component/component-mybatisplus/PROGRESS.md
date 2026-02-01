# component-mybatisplus 开发进度

> 维护规则：本文件用于开发过程持续更新，不删历史记录，只追加。

## 1. 当前状态
- 状态：开发中
- 当前迭代：V1
- 最后更新：2026-01-31

## 2. 里程碑

- [ ] V1.0 基础能力
- [ ] V1.1 动态权限规则引擎基础版
- [ ] V1.2 多租户混合模式
- [ ] V1.3 安全护栏完善

## 3. 任务清单（滚动维护）

### 3.1 需求与设计
- [x] 需求文档完成
- [x] 最终方案契约完成
- [ ] 详细设计文档（包结构/接口/时序）
- [x] 数据结构与 JSON DSL 规则定义

### 3.2 基础框架
- [x] AutoConfiguration 结构与开关定义
- [x] MyBatis Plus 拦截器链组装
- [x] 统一日志格式与 SQL 日志

### 3.3 多租户
- [x] TenantContext/AccessContext 定义
- [x] TenantResolver/TenantProfileStore 接口与默认实现
- [x] dynamic-datasource 路由注入点（Filter/Aspect）
- [x] TenantLineHandler 实现

### 3.4 动态权限
- [x] ResourceResolver 接口与默认实现
- [x] PolicyRepository 接口与默认实现
- [x] JSON DSL 解析与过滤器构建
- [x] 行/列约束输出与拦截执行
- [x] DataPermissionHandler 注入与拦截器对接

### 3.5 审计与安全
- [x] MetaObjectHandler 自动填充
- [x] BlockAttackInnerInterceptor 启用

### 3.6 测试与回归
- [ ] 多租户混合模式测试
- [ ] 动态权限命中/未命中测试
- [ ] join/union 权限拼接测试
- [ ] 上下文清理测试

## 4. 变更记录

- 2026-01-29：创建 README 主文档（含需求与契约），初始化进度文档。
- 2026-01-29：明确动态权限仅查询生效、无标识不拦截，并补充默认开关说明。
- 2026-01-29：新增多租户/权限/审计/观测基础结构与默认实现，完成拦截器链装配。
- 2026-01-31：重构数据权限为 framework-security SRA 模型，替换原有规则与标识设计。
- 2026-01-31：新增 data_resource/data_resource_policy DDL（MySQL/PostgreSQL/SQLite）与 DataResourceResolver 落地资源解析。
- 2026-01-31：租户配置支持数据库数据源管理（jdbc_url 等字段），并在路由阶段优先注册数据源。
- 2026-01-31：更新 db_init.sql 以覆盖 MySQL/PostgreSQL/SQLite，并对齐租户/资源表字段与审计字段。
- 2026-01-31：新增租户数据源类型枚举（TenantDataSourceType），替换 ds_type 字段为枚举模型。

## 5. framework-security 重构

### 5.1 设计与契约
- [x] 权限模型迁移为 SRA 与策略决策
- [x] README 与执行管线对齐 framework-security
- [x] 删除旧规则/标识/编译器模型

### 5.2 代码实现
- [x] 适配 AuthorizationService + PolicyEngine 流程
- [x] JSON DSL 解析与过滤器构建
- [x] 默认 SubjectResolver / ResourceResolver
- [x] PolicyRepository 具体存储实现（按业务表落地）
- [x] PermissionChangePublisher/Subscriber 默认链路
