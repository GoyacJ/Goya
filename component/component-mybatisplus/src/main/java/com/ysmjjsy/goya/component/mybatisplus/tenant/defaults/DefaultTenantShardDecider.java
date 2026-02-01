package com.ysmjjsy.goya.component.mybatisplus.tenant.defaults;

import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileStore;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantShardDecider;
import lombok.RequiredArgsConstructor;

/**
 * <p>默认租户模式决策器</p>
 *
 * <p>优先使用租户配置，缺失时使用默认模式。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@RequiredArgsConstructor
public class DefaultTenantShardDecider implements TenantShardDecider {

    private final TenantProfileStore profileStore;
    private final GoyaMybatisPlusProperties.Tenant tenantOptions;

    /**
     * 决策租户模式。
     *
     * @param tenantId 租户 ID
     * @return 租户模式
     */
    @Override
    public TenantMode decide(String tenantId) {
        TenantProfile profile = profileStore.load(tenantId);
        if (profile != null && profile.mode() != null) {
            return profile.mode();
        }
        return tenantOptions.defaultMode();
    }
}
