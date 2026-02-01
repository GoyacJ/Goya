package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户配置存储</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public interface TenantProfileStore {

    /**
     * 加载租户配置。
     *
     * @param tenantId 租户 ID
     * @return 租户配置
     */
    TenantProfile load(String tenantId);

    /**
     * 获取租户配置版本。
     *
     * @param tenantId 租户 ID
     * @return 版本号
     */
    long version(String tenantId);
}
