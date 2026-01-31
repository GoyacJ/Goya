package com.ysmjjsy.goya.component.mybatisplus.tenant.defaults;

import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceRouter;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;

/**
 * <p>默认数据源路由器</p>
 *
 * <p>共享库模式返回固定 dsKey，独库模式按 tenantId 拼接。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public class DefaultTenantDataSourceRouter implements TenantDataSourceRouter {

    private static final String CORE_DS_KEY = "core";
    private static final String DEDICATED_PREFIX = "tenant_";

    /**
     * 路由数据源。
     *
     * @param tenantId 租户 ID
     * @param mode 模式
     * @return 数据源 key
     */
    @Override
    public String route(String tenantId, TenantMode mode) {
        if (mode == TenantMode.DEDICATED_DB && tenantId != null && !tenantId.isBlank()) {
            return DEDICATED_PREFIX + tenantId;
        }
        return CORE_DS_KEY;
    }
}
