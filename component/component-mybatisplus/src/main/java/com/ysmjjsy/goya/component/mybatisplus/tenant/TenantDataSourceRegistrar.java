package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户数据源注册器。</p>
 *
 * <p>用于根据租户配置动态注册数据源。</p>
 *
 * @author goya
 * @since 2026/1/31 12:30
 */
public interface TenantDataSourceRegistrar {

    /**
     * 注册数据源并返回 dsKey。
     *
     * @param tenantId 租户 ID
     * @param profile 数据源配置
     * @param dsKeyHint dsKey 提示（可为空）
     * @return 数据源 key
     */
    String register(String tenantId, TenantDataSourceProfile profile, String dsKeyHint);
}
