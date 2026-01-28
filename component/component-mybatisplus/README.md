component-mybatisplus 最终方案契约

0. 目标与非目标

目标

在 Spring Boot 4.0.2 + JDK 25 环境下，提供单模块 component-mybatisplus，实现企业级数据访问治理：
1.	多租户混合模式：核心库列隔离 + 大租户独库（dynamic-datasource 路由 + MP TenantLine）。
2.	动态数据权限：不依赖配置文件定义权限规则；权限完全由“用户配置/规则存储”驱动，运行时可变更且可快速生效（版本 + 缓存）。
3.	通用字段自动填充：审计字段与租户字段自动填充（MetaObjectHandler）。
4.	安全护栏：阻断无 WHERE 的 update/delete（BlockAttack）。
5.	可观测性：慢 SQL、traceId 关联（不改写 SQL）。

非目标（明确不做）
•	不提供完整 IAM/权限管理 UI 与业务授权流程（本模块只消费规则）。
•	不自研 SQL 改写器：租户、权限的 SQL 注入点均基于 MyBatis-Plus 官方插件（TenantLine/DataPermission）。
•	不接管 dynamic-datasource 的数据源管理与读写分离；仅提供 tenant→dsKey 的标准化决策与注入点。

⸻

1. 依赖约束

核心依赖（必须）
•	com.baomidou:mybatis-plus-spring-boot4-starter
•	com.baomidou:dynamic-datasource-spring-boot4-starter

实现原则
•	只在“必要且稳定”的地方新增依赖。
•	对 Spring Web / Spring Security 采取 可选集成（classpath 存在才启用自动解析），不强制引入。

⸻

2. 总体执行管线（固定顺序）

2.1 请求生命周期（必须固定）
1.	进入请求 / 进入业务调用之前

	•	解析 tenantId
	•	决策租户落库模式（核心共享 / 独库）
	•	路由到 dynamic-datasource 的 dsKey 并设置上下文（必须发生在事务开始之前）
	•	建立 AccessContext（用户画像：userId、subjectId、属性集合等）

	2.	MyBatis-Plus 执行阶段（interceptor chain）
按顺序执行：

	1.	BlockAttackInnerInterceptor（安全护栏）
	2.	TenantLineInnerInterceptor（租户列隔离，按 mode 可开关/忽略策略）
	3.	DataPermissionInterceptor（动态权限 where 追加）
	4.	（可选）Pagination（若业务启用）
	5.	Observability interceptor（仅观测，不改写 SQL）

	3.	请求结束

	•	清理 dynamic-datasource 上下文
	•	清理 TenantContext / AccessContext（必须 finally）

⸻

3. 上下文模型（统一入口，禁止散装 ThreadLocal）

3.1 TenantContext（必需）

职责：提供当前线程的租户信息与路由结果。

字段：
•	tenantId: String
•	mode: TenantMode（枚举：CORE_SHARED, DEDICATED_DB）
•	dsKey: String（dynamic-datasource 使用）

行为：
•	set(TenantContextValue v) / get() / clear()

约束：
•	任何 DB 操作前必须存在 TenantContext（生产默认 requireTenant=true）。
•	dsKey 必须在事务开始前确定。

3.2 AccessContext（必需）

职责：提供当前线程的用户画像与授权主体信息（subject）。

字段：
•	subjectId: String（用于加载规则集：可为 userId、roleId、或组合主体）
•	userId: String（审计与常用变量）
•	attributes: Map<String, Object>（如 deptIds、regionCodes、roleCodes、projectIds 等）

行为：
•	set(AccessContextValue v) / get() / clear()

约束：
•	subjectId 是规则加载与缓存的唯一主键之一。
•	attributes 的 value 只能是可序列化且可类型校验的结构（String/Number/Collection等）。

⸻

4. 多租户混合模式契约

4.1 租户解析

接口：TenantResolver
•	String resolveTenantId()

默认策略（可选 web 集成）：
•	优先从 TenantContext 已设置值读取
•	若 classpath 存在 Spring Web，则从 Header（默认 X-Tenant-Id）读取

4.2 租户落库模式决策

接口：TenantShardDecider
•	TenantMode decide(String tenantId)

语义：
•	决策租户属于核心共享库（列隔离）还是独库。

默认实现策略：
•	由 RuleStore/租户配置表或缓存加载（见 4.4）

4.3 数据源路由

接口：TenantDataSourceRouter
•	String route(String tenantId, TenantMode mode)

语义：
•	输出 dynamic-datasource 的 dsKey。
•	对 CORE_SHARED 返回核心库 dsKey（例如 core）。
•	对 DEDICATED_DB 返回专属 dsKey（例如 tenant_10001 或按分组 group_a）。

4.4 租户配置存储（动态）

接口：TenantProfileStore
•	TenantProfile load(String tenantId)
•	long version(String tenantId)（或 updatedAt）

TenantProfile 必含：
•	mode
•	dsKey
•	（可选）tenantLineEnabled：独库是否仍追加 tenant_id 条件（推荐默认 true）

缓存策略：
•	L2（Caffeine）：tenantId → TenantProfile（TTL + version 校验）
•	version 变化则立即重载

4.5 dynamic-datasource 上下文注入点

组件提供一种注入方式（必须）：
•	若 classpath 存在 Spring Web：注册 OncePerRequestFilter
•	若不存在 Web：提供 TenantRoutingAspect（可选，供非 Web 服务使用）

约束：
•	注入点必须在事务开始前执行。
•	finally 必须清理 dynamic-datasource 上下文、TenantContext。

4.6 租户列隔离（MyBatis-Plus）

使用 MP 官方：
•	TenantLineInnerInterceptor
•	TenantLineHandler

Handler 合约：
•	getTenantIdColumn() 默认 tenant_id
•	getTenantId() 从 TenantContext 读取
•	ignoreTable(table) 支持静态忽略表（公共字典表）与动态忽略：
•	若当前 mode=DEDICATED_DB 且 tenantLineEnabled=false，则全表忽略（不追加 tenant 条件）

⸻

5. 动态数据权限契约（不依赖配置文件）

5.1 核心原则
•	权限规则来自用户配置（DB/配置中心），运行时可变更。
•	用户配置为结构化规则，禁止 raw SQL 直通。
•	规则必须可编译为安全谓词（Predicate AST），再生成 where 条件片段。
•	SQL 应用依赖 MP 官方 DataPermissionInterceptor + MultiDataPermissionHandler。

5.2 资源模型（强制引入，保证通用性）
•	Resource：逻辑资源（如 ORDER/CUSTOMER）
•	ResourceMapping：
•	resource → tables（一个资源可对应多表）
•	resourceField → column（字段映射白名单）

接口：ResourceRegistry
•	String resolveResource(String tableName, String mappedStatementId)
•	ColumnRef resolveColumn(String resource, String fieldKey)

ColumnRef：
•	table: String（可选）
•	column: String（必须，符合列名白名单正则）

约束：
•	用户配置只能使用 resource 与 fieldKey，不能直接写 column。

5.3 规则存储与版本

接口：PermissionRuleStore
•	RuleSet load(String tenantId, String subjectId, String resource)
•	long version(String tenantId, String subjectId)（或 per resource 版本）

RuleSet 至少包含：
•	rules: List
•	updatedAt/version

缓存策略（必须）：
•	L1（请求内）：同一请求对同一 subject/resource 只编译一次
•	L2（Caffeine）：(tenantId, subjectId, resource, version) → CompiledPredicate
•	version 变化 -> 失效并重载

5.4 规则表达（结构化）

Rule 结构（存储层）：
•	subjectId
•	resource
•	effect: ALLOW/DENY（可选，第一版可只做 ALLOW）
•	predicates: List
•	combine: AND/OR
•	priority

PredicateDef 支持（第一版企业常用且安全）：
•	EQ（string/number）
•	IN（string/number list）
•	BETWEEN（number/date）
•	LIKE（受限：仅前缀/后缀匹配，防全表扫）
•	EXISTS（可选，若引入则必须受限模板）

变量引用：
•	${userId}、${deptIds}、${regionCodes} 等，从 AccessContext.attributes 取值
•	变量类型必须匹配 Predicate 类型，否则规则无效并记录审计日志（failClosed 时整条访问拒绝）

5.5 编译器与生成器

接口：PermissionCompiler
•	CompiledPredicate compile(RuleSet ruleSet, AccessContextValue access, ResourceRegistry registry)

CompiledPredicate：
•	String toWhereSql(String tableName)（或直接输出 Expression）
•	Explain explain()（可选，用于审计与排障）

安全约束：
•	column 只能来自 ResourceRegistry（白名单）
•	字面量必须转义/类型校验
•	编译失败策略：
•	failClosed=true：返回 1=0
•	failClosed=false：忽略该规则，但记录告警日志

5.6 SQL 应用（MyBatis-Plus）

使用 MP 官方：
•	DataPermissionInterceptor
•	MultiDataPermissionHandler#getSqlSegment(Table table, Expression where, String mappedStatementId)

Handler 行为：
1.	resolve tenantId/mode from TenantContext
2.	resolve resource via ResourceRegistry(tableName, msId)
3.	load compiled predicate via store+cache
4.	输出 whereSql，并通过 parseCondExpression 解析为 Expression 后与原 where AND

applyToWrite：
•	开关控制对 UPDATE/DELETE 是否追加权限条件。

忽略机制：
•	业务侧使用 MP 官方 @InterceptorIgnore(dataPermission="true") 进行语句级绕过。

⸻

6. 审计字段自动填充契约

使用 MP 扩展点：
•	MetaObjectHandler

字段规范（默认，可配置）：
•	created_at, created_by, updated_at, updated_by, tenant_id

接口：
•	AuditorProvider：current user id/name（可选 security 集成）
•	TimeProvider：统一时间源（Instant）

行为：
•	insert：填 created/updated + by + tenant_id
•	update：填 updated + by

⸻

7. 安全护栏契约

使用 MP 官方：
•	BlockAttackInnerInterceptor（阻断全表 update/delete）

默认开启（可配置）。

⸻

8. 可观测性契约

提供 InnerInterceptor（不改写 SQL）：
•	记录耗时、慢 SQL、traceId、mappedStatementId
•	参数日志默认关闭（可配置）
•	traceId 来源接口：SqlTraceProvider（默认尝试 MDC）

⸻

9. 自动装配与开关策略

9.1 自动装配
•	Spring Boot 4：AutoConfiguration.imports 注册自动配置类
•	所有扩展点 Bean 使用 @ConditionalOnMissingBean，允许上层覆盖

9.2 关键开关（最少集）
•	goya.mybatis-plus.tenant.enabled
•	goya.mybatis-plus.tenant.require-tenant
•	goya.mybatis-plus.permission.enabled
•	goya.mybatis-plus.permission.fail-closed
•	goya.mybatis-plus.permission.apply-to-write
•	goya.mybatis-plus.safety.block-attack
•	goya.mybatis-plus.observability.enabled

注意：权限规则本身不通过配置文件表达，配置文件只控制开关与一些默认策略。

⸻

10. 失败策略（企业默认安全）

默认策略建议：
•	tenant 缺失：直接拒绝（requireTenant=true）
•	AccessContext 缺失：
•	permission.failClosed=true：where = 1=0
•	规则编译失败：
•	failClosed=true：1=0
•	failClosed=false：忽略该规则并告警

⸻

11. 扩展点清单（最终固定，不再扩张）

必须保留：
•	TenantResolver
•	TenantShardDecider
•	TenantDataSourceRouter
•	TenantProfileStore
•	ResourceRegistry
•	PermissionRuleStore
•	PermissionCompiler
•	AuditorProvider
•	TimeProvider
•	SqlTraceProvider

允许上层覆盖：任意一个。

禁止新增：
•	自定义 ignore 注解（统一使用 MP 官方 @InterceptorIgnore）
•	自研 SQL 改写器

⸻

12. 测试契约（实现必须满足）

必须覆盖：
•	混合租户：同一请求内 dsKey 与 tenant 条件一致
•	独库 tenantLineEnabled=false 时不追加 tenant 条件
•	动态权限：规则变更 version 后在 TTL 内可感知并生效
•	join / 子查询 / union 的 where 拼接可被解析（parseCondExpression 成功）
•	applyToWrite=true 时 update/delete 同样受权限限制
•	BlockAttack 生效
•	finally 清理上下文（防线程复用污染）

⸻

这份契约如果你认可，我们后续实现阶段会严格按此分工推进：
1.	上下文与路由（TenantProfileStore + Router + Filter/Aspect）
2.	租户插件对接（TenantLineHandler）
3.	动态权限引擎（ResourceRegistry + RuleStore + Compiler + DataPermissionHandler）
4.	审计填充 + 安全护栏 + 观测
5.	测试矩阵与回归用例

你不需要再补“想法清单”，因为契约已经把每个想法的归属（做/不做/怎么做）钉死了。