package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户数据源路由器</p>
 * 将 tenantId + mode 转换为 dynamic-datasource 使用的 dsKey。
 *
 * <p><b>语义：</b>
 * <ul>
 *   <li>CORE_SHARED 返回核心库 dsKey（例如 core）</li>
 *   <li>DEDICATED_DB 返回租户专属 dsKey（例如 tenant_10001 或 group_a）</li>
 * </ul>
 * @author goya
 * @since 2026/1/28 21:50
 */
public interface TenantDataSourceRouter {

    /**
     * 输出 dynamic-datasource 的 dsKey。
     *
     * @param tenantId 租户 ID
     * @param mode 租户模式
     * @return 数据源键 dsKey
     */
    String route(String tenantId, TenantMode mode);
}