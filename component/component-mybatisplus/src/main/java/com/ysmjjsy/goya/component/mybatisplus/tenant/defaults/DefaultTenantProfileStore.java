package com.ysmjjsy.goya.component.mybatisplus.tenant.defaults;

import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileStore;

/**
 * <p>默认租户配置存储</p>
 *
 * <p>返回共享库 + 启用 tenantLine 的默认配置。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public class DefaultTenantProfileStore implements TenantProfileStore {

    /**
     * 加载租户配置。
     *
     * @param tenantId 租户 ID
     * @return 租户配置
     */
    @Override
    public TenantProfile load(String tenantId) {
        return new TenantProfile(tenantId, TenantMode.CORE_SHARED, "core", true, 0L);
    }

    /**
     * 获取版本号。
     *
     * @param tenantId 租户 ID
     * @return 版本号
     */
    @Override
    public long version(String tenantId) {
        return 0L;
    }
}
