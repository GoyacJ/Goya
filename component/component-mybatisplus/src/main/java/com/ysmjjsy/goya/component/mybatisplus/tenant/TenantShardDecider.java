package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户落库模式决策器</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public interface TenantShardDecider {

    /**
     * 决策租户模式。
     *
     * @param tenantId 租户 ID
     * @return 租户模式
     */
    TenantMode decide(String tenantId);
}
