# component-mybatisplus

> 本 README 合并需求定义与最终方案契约，作为后续开发的唯一依据。
> 当前版本以 **framework-security 的 SRA 设计** 为权威模型。

---

## 需求定义

### 1. 背景与目标

Goya 需要一个企业级 MyBatis Plus 组件，统一数据访问治理，并具备“多租户混合模式”和“数据权限执行”能力。

核心目标：
1. **企业级 MyBatis Plus 配置**：统一插件、拦截器、审计字段、日志、安全护栏等能力。
2. **数据权限执行**：基于 framework-security 的 SRA 与策略模型，在查询阶段进行行级过滤。
3. **多租户混合模式**：同时支持租户列隔离与独立数据库（动态路由）。

非目标（明确不做）：
- 不提供完整 IAM/权限管理 UI，本模块仅执行策略。
- 不自研 SQL 改写器，优先基于 MyBatis Plus 官方机制扩展。

### 2. 范围定义

#### 2.1 包含
- MyBatis Plus 企业级基础配置（分页、审计字段、逻辑删除、安全护栏、日志/慢 SQL）。
- 基于 framework-security 的数据权限执行（Policy → DSL → SQL 条件）。
- 多租户混合模式（租户上下文、数据源路由、tenant_id 拦截）。

#### 2.2 不包含
- 权限策略配置界面与授权流程（由平台应用层完成）。
- 复杂 BI 级 SQL 的全覆盖（第一期聚焦常见 CRUD + 常见查询）。

### 3. 术语与核心概念（SRA）

- **主体（Subject）**：用户/角色/团队/组织。
- **资源（Resource）**：表/字段/API/文件等。
- **操作（Action）**：QUERY/CREATE/UPDATE/DELETE 等。
- **策略（Policy）**：主体对资源的操作规则（允许/拒绝、范围、过期、继承）。
- **DSL**：行级过滤表达式，转换为 SQL 条件。

### 4. 使用场景

1. 业务侧通过权限中心配置策略并下发（framework-security 控制面）。
2. 业务查询触发 MyBatis Plus 拦截器，解析 Subject/Resource/Action。
3. 策略引擎输出决策与行级过滤条件，查询结果自动过滤。

### 5. 功能需求

#### 5.1 企业级 MyBatis Plus 配置
- 插件配置：分页、BlockAttack、数据权限拦截、多租户拦截、观测拦截（慢 SQL）。
- 审计字段填充：created_by/created_at/updated_by/updated_at + tenant_id 自动填充。
- 统一日志格式与 traceId 关联（不修改 SQL）。

#### 5.2 数据权限执行（核心）

**适用范围**：仅对查询（SELECT）生效，不负责写入权限。

##### 5.2.1 核心流程
1. 请求进入后构建 SubjectContext（来自 AccessContext）。
2. 查询发生时，根据表名与语句 ID 构建 ResourceContext。
3. 通过 AuthorizationService 调用策略引擎完成鉴权。
4. 若允许且存在 DSL，生成 SQL 条件并追加到 WHERE。
5. 若拒绝，返回 1=0（安全默认）。

##### 5.2.2 DSL 规则
- DSL 为结构化表达式，禁止 raw SQL 直通。
- 本模块默认使用 JSqlParser 解析 DSL。
- 语法与 AST 定义以 framework-security 为准。

### 6. 多租户混合模式

#### 6.1 模式
- **共享库模式**：所有租户共享数据库，通过 tenant_id 列隔离。
- **独立库模式**：大租户独享数据库，通过动态数据源路由。
- 支持混合模式：不同租户采用不同模式。

#### 6.2 关键流程
1. 解析 tenantId（Header / Token / Context）。
2. 决策租户模式（共享库 / 独立库）。
3. 路由到指定数据源（dynamic-datasource）。
4. 若共享库模式，则追加 tenant_id 条件。
5. 请求结束清理上下文。

---

## 最终方案契约

### 1. 依赖约束
核心依赖（必须）：
- com.baomidou:mybatis-plus-spring-boot4-starter
- com.baomidou:dynamic-datasource-spring-boot4-starter
- com.ysmjjsy.goya:framework-security

实现原则：
- 只在必要且稳定的地方新增依赖。
- 对 Spring Web / Spring Security 采取可选集成。

### 2. 总体执行管线（固定顺序）

**请求生命周期**（必须固定）
1. 进入请求 / 进入业务调用之前
   - 解析 tenantId
   - 决策租户落库模式
   - 路由 dsKey 并设置上下文（事务开始前）
   - 建立 AccessContext（subjectId + subjectType + userId + attributes）
2. MyBatis-Plus 执行阶段（interceptor chain）
   1) BlockAttackInnerInterceptor
   2) TenantLineInnerInterceptor
   3) DataPermissionInterceptor
   4) Pagination（可选）
   5) Observability interceptor
3. 请求结束
   - 清理 dynamic-datasource 上下文
   - 清理 TenantContext / AccessContext

### 3. 上下文模型（统一入口）

#### 3.1 TenantContext
字段：
- tenantId: String
- mode: TenantMode（CORE_SHARED, DEDICATED_DB）
- dsKey: String

#### 3.2 AccessContext
字段：
- subjectId: String
- subjectType: SubjectType
- userId: String
- attributes: Map<String, Object>

### 4. 动态数据权限（与 framework-security 对齐）

#### 4.1 关键接口
- SubjectResolver
- ResourceResolver
- PolicyRepository
- RangeDslParser
- RangeFilterBuilder
- PolicyEngine
- AuthorizationService

#### 4.2 默认实现
- SubjectResolver：从 AccessContext 构造 Subject
- ResourceResolver：基于 data_resource 表解析资源与父子关系
- RangeDslParser：JSqlParser 条件解析
- RangeFilterBuilder：JSqlParser Expression 输出
- PolicyRepository：默认读取 data_resource_policy 表（可覆盖）

补充说明：
- data_resource 中 `resource_type=FIELD` 的记录用于 DSL 字段白名单校验（父资源为表）。

#### 4.3 执行策略
- Deny 优先，默认拒绝
- 明确拒绝返回 1=0
- 解析/执行异常按 failClosed 决策

### 5. 审计字段自动填充
- MetaObjectHandler 自动填充 created/updated + tenant_id
- AuditorProvider / TimeProvider 可覆盖

### 6. 安全护栏
- BlockAttackInnerInterceptor 默认开启

### 7. 可观测性
- 记录耗时、慢 SQL、traceId、mappedStatementId
- 不改写 SQL

### 8. 自动装配与开关
关键开关：
- goya.mybatis-plus.tenant.enabled
- goya.mybatis-plus.tenant.require-tenant
- goya.mybatis-plus.permission.enabled
- goya.mybatis-plus.permission.fail-closed
- goya.mybatis-plus.permission.apply-to-write
- goya.mybatis-plus.safety.block-attack
- goya.mybatis-plus.observability.enabled

### 9. 失败策略
- tenant 缺失：requireTenant=true 时拒绝
- AccessContext 缺失：failClosed=true 时返回 1=0
- 解析失败：failClosed=true 时返回 1=0

### 9.1 DDL 支持
- `db_init.sql` 已提供 MySQL / PostgreSQL / SQLite 三种版本。

### 10. 扩展点清单（固定）
- SubjectResolver
- ResourceResolver
- PolicyRepository
- RangeDslParser
- RangeFilterBuilder
- AuditorProvider
- TimeProvider
- SqlTraceProvider

禁止新增：
- 自定义 ignore 注解（统一使用 MP 官方 @InterceptorIgnore）
- 自研 SQL 改写器

---

如需新增适配（JPA、Spring Security 等），应在独立模块实现 framework-security 的 SPI，
component-mybatisplus 只负责 MyBatis Plus 生态内的执行与拦截。
