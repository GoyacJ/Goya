# 多租户需求 | Multi-Tenancy Requirements

## 1. 功能概述

实现企业级多租户系统，支持数据隔离、配置隔离、独立部署等能力。

## 2. 核心功能

### 2.1 租户识别

**识别方式**（优先级）：
1. Path: `/t/{tenant}/api/users`
2. Subdomain: `tenant-001.example.com`
3. Header: `X-Tenant-ID`

### 2.2 数据隔离

**隔离级别**：
- **共享表**：添加 `tenant_id` 字段（推荐）
- **独立表**：每个租户独立表
- **独立库**：每个租户独立数据库

### 2.3 配置隔离

**隔离维度**：
- OAuth2 Issuer
- JWK 签名密钥
- 登录方式配置
- 主题定制

### 2.4 租户管理

**功能**：
- 租户创建/删除
- 租户配置管理
- 租户用量统计
- 租户计费（预留）

## 3. 技术实现

### 3.1 租户上下文

```java
public class TenantContext {
    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();
    
    public static void setTenant(String tenant) {
        TENANT.set(tenant);
    }
    
    public static String getTenant() {
        return TENANT.get();
    }
    
    public static void clear() {
        TENANT.remove();
    }
}
```

### 3.2 数据隔离（MyBatis Plus）

```java
@Component
public class TenantLineHandler implements TenantLineHandler {
    @Override
    public Expression getTenantId() {
        return new StringValue(TenantContext.getTenant());
    }
    
    @Override
    public String getTenantIdColumn() {
        return "tenant_id";
    }
}
```

## 4. 用户故事

**作为**SaaS 平台运营者  
**我想要**完全的租户数据隔离  
**以便于**保证数据安全和合规

**验收标准**：
- 租户A无法访问租户B的数据
- 租户A无法使用租户B的Token
- 支持租户独立配置

## 5. 参考资料

- [Multi-Tenancy Patterns](https://learn.microsoft.com/en-us/azure/architecture/patterns/multitenancy)
