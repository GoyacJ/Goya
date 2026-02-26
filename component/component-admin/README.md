# component-admin

`component-admin` 提供企业级后台管理域能力，按单 Jar 形式集成到现有 Goya 安全体系。

## 模块定位

- 默认接管安全 SPI：`IUserService`、`IRolePermissionService`、`ITenantService`
- 提供管理域核心对象：用户、角色、权限、菜单、部门、字典
- 与 `framework-security`、`component-mybatisplus` 联动实现 RBAC + 动态策略双轨
- 支持权限变更后的会话撤销与策略广播
- 管理写操作默认按 `tenantId` 严格校验，拒绝跨租户关联写入

## 核心能力

- 用户：查询、新增、修改、删除、状态、密码、角色绑定、部门绑定、设备管理
- 角色：查询、新增、修改、删除、权限绑定、菜单绑定、数据范围
- 权限：查询、新增、修改、删除
- 菜单：树查询、新增、修改、删除、循环父子校验
- 部门：树查询、新增、修改、删除、循环父子校验
- 字典：类型与条目全量 CRUD（租户隔离）
- 策略：`data_resource` / `data_resource_policy` 管理 API
- 引导：可选初始化 `SUPER_ADMIN` 角色和管理员账号

## API 清单

前缀：`/api/admin`

- 用户
  - `GET /users`
  - `GET /users/{id}`
  - `POST /users`
  - `PUT /users/{id}`
  - `DELETE /users/{id}`
  - `PUT /users/{id}/status`
  - `PUT /users/{id}/password`
  - `PUT /users/{id}/roles`
  - `PUT /users/{id}/depts`
  - `GET /users/{id}/devices`
  - `DELETE /users/{id}/devices/{deviceId}`
- 角色
  - `GET /roles`
  - `GET /roles/{id}`
  - `POST /roles`
  - `PUT /roles/{id}`
  - `DELETE /roles/{id}`
  - `PUT /roles/{id}/permissions`
  - `PUT /roles/{id}/menus`
  - `PUT /roles/{id}/data-scope`
- 权限
  - `GET /permissions`
  - `GET /permissions/{id}`
  - `POST /permissions`
  - `PUT /permissions/{id}`
  - `DELETE /permissions/{id}`
- 菜单
  - `GET /menus/tree`
  - `GET /menus/{id}`
  - `POST /menus`
  - `PUT /menus/{id}`
  - `DELETE /menus/{id}`
- 部门
  - `GET /depts/tree`
  - `GET /depts/{id}`
  - `POST /depts`
  - `PUT /depts/{id}`
  - `DELETE /depts/{id}`
- 字典
  - `GET /dict/types`
  - `POST /dict/types`
  - `PUT /dict/types/{id}`
  - `DELETE /dict/types/{id}`
  - `GET /dict/types/{typeCode}/items`
  - `POST /dict/types/{typeCode}/items`
  - `PUT /dict/items/{id}`
  - `DELETE /dict/items/{id}`
- 策略与资源
  - `GET /policies`
  - `POST /policies`
  - `PUT /policies/{id}`
  - `DELETE /policies/{id}`
  - `GET /resources`
  - `POST /resources`
  - `PUT /resources/{id}`
  - `DELETE /resources/{id}`

## 配置项

前缀：`goya.admin`

- `enabled=true`
- `default-tenant-id=public`
- `tenant-header=X-Tenant-Id`
- `tenant-issuer-template=`
- `bootstrap.enabled=true`
- `bootstrap.admin-username=admin`
- `bootstrap.admin-password=${GOYA_ADMIN_BOOTSTRAP_PASSWORD:}`
- `bootstrap.super-role-code=SUPER_ADMIN`
- `rbac.cache-enabled=true`
- `rbac.cache-ttl=PT10M`
- `policy.sync-enabled=true`
- `session.revoke-on-role-change=true`

## 数据库初始化

- 初始化脚本：`src/main/resources/db_init.sql`
- 覆盖数据库：MySQL / PostgreSQL / SQLite
- 表前缀统一：`iam_`

## 构建校验

```bash
mvn -pl component/component-admin -am -DskipTests validate
```

全仓编译（JDK25）：

```bash
mvn -DskipTests compile
```
