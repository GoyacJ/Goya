package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户落库模式枚举</p>
 * 用于描述当前租户的数据存储形态，并作为后续数据源路由与租户列隔离策略的依据。
 *
 * <ul>
 *   <li>{@link #CORE_SHARED}：核心共享库（同库多租户），通过租户列隔离实现数据隔离</li>
 *   <li>{@link #DEDICATED_DB}：独立数据库（大租户独库），通过 dynamic-datasource 路由至专属数据源</li>
 * </ul>
 *
 * <p><b>约束：</b>
 * <ul>
 *   <li>模式决策必须在事务开始前完成</li>
 *   <li>模式一旦确定，在同一请求/调用链中不得变化</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 21:48
 */
public enum TenantMode {

    /** 核心共享库：通过 tenant_id 等租户列实现隔离。 */
    CORE_SHARED,

    /** 独立数据库：通过 dynamic-datasource 路由到专属数据源。 */
    DEDICATED_DB
}
