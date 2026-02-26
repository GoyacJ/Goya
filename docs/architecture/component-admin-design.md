# component-admin 设计说明

## 1. 目标

`component-admin` 作为企业管理域默认实现，提供：

- 单模块交付（`component/component-admin`）
- 安全 SPI 默认接管（可覆盖）
- RBAC 主链 + 动态策略副链
- 会话撤销、权限广播、超管首启引导闭环

## 2. 模块边界

复用模块：

- `framework-boot-starter`：框架基础能力入口
- `component-mybatisplus`：实体基类、策略表与 mapper、租户 profile store
- `security-core`：`IUserService` / `IRolePermissionService` / `ITenantService`
- `framework-security`：`PermissionChangePublisher` 与策略域模型

不新增并行同类模块，不引入新第三方依赖。

## 3. 包结构

根包：`com.ysmjjsy.goya.component.admin`

- `configuration`：自动配置
- `configuration.properties`：`AdminProperties`
- `constants` / `enums` / `error`
- `security`：SPI 默认实现
- `support`：租户、树、密码、会话撤销辅助
- `bootstrap`：超级管理员初始化
- `user` / `role` / `permission` / `menu` / `dept` / `dict` / `policy`
  - 每个子域包含 `entity` / `mapper` / `dto` / `service` / `controller`

## 4. 数据模型

DDL 文件：`component/component-admin/src/main/resources/db_init.sql`

主要表：

- `iam_user` / `iam_user_password_history` / `iam_user_device` / `iam_user_auth_audit_log`
- `iam_role` / `iam_permission`
- `iam_user_role` / `iam_role_permission` / `iam_role_menu`
- `iam_menu`
- `iam_dept` / `iam_user_dept` / `iam_role_dept`
- `iam_dict_type` / `iam_dict_item`

约束：

- 用户：`tenant_id + username`
- 角色：`tenant_id + role_code`
- 权限：`tenant_id + permission_code`
- 字典类型：`tenant_id + dict_type_code`
- 字典条目：`tenant_id + dict_type_code + item_code`

## 5. 安全 SPI 默认接管

- `AdminUserService`：实现 `IUserService`
- `AdminRolePermissionService`：实现 `IRolePermissionService`
- `AdminTenantService`：实现 `ITenantService`

三者均使用 `@ConditionalOnMissingBean`，业务可替换。

## 6. RBAC + 策略双轨

RBAC 主链：

- 用户-角色-权限用于签发 claims（`roles`、`authorities`）

策略副链：

- 角色权限变更触发 `syncRolePermissionPolicies`
- 同步写入 `data_resource_policy`（`subject_type=ROLE`、`resource_type=API`）
- 通过 `PermissionChangePublisher` 发布变更事件

## 7. 会话闭环

- 角色权限变更、用户禁用/密码变更时触发会话撤销
- 调用 `SecuritySessionLifecycleService.revokeByUser(tenantId, userId)`
- 资源侧继续由 `RevokedTokenFilter` 实时拦截旧 token

## 8. 多租户隔离

- 管理域写路径默认按 `tenantId` 强制约束，不做跨租户回退查询
- 角色/权限/菜单/部门关联关系在写入前进行租户内存在性校验
- 策略与资源更新接口按 `tenantId + id` 组合定位，拒绝跨租户更新
## 9. 引导机制

`AdminBootstrapRunner`：

- 可选初始化 `SUPER_ADMIN` 角色
- 可选初始化管理员账号（密码取环境变量占位）
- 尝试为超管同步 API 访问策略，避免首启策略缺失

## 10. 配置

前缀：`goya.admin`

- `enabled`
- `default-tenant-id`
- `tenant-header`
- `tenant-issuer-template`
- `bootstrap.*`
- `rbac.*`
- `policy.sync-enabled`
- `session.revoke-on-role-change`

## 11. 验收口径

- 模块编译：`mvn -pl component/component-admin -am -DskipTests validate`
- 全仓编译：`mvn -DskipTests compile`（JDK25）
- 文档与代码保持同步更新。
