package com.ysmjjsy.goya.component.mybatisplus.tenant.defaults;

import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantShardDecider;

/**
 * <p>默认租户模式决策器</p>
 *
 * <p>默认返回共享库模式。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public class DefaultTenantShardDecider implements TenantShardDecider {

    /**
     * 决策租户模式。
     *
     * @param tenantId 租户 ID
     * @return 租户模式
     */
    @Override
    public TenantMode decide(String tenantId) {
        return TenantMode.CORE_SHARED;
    }
}
