package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户数据源路由器</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public interface TenantDataSourceRouter {

    /**
     * 生成数据源 Key。
     *
     * @param tenantId 租户 ID
     * @param mode     租户模式
     * @return 数据源 Key
     */
    String route(String tenantId, TenantMode mode);
}
