package com.ysmjjsy.goya.component.mybatisplus.tenant.profile;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfile;

/**
 * <p>租户画像配置服务</p>
 * <p>
 * 对外提供面向治理的“读模型” {@link TenantProfile}，避免 controller/业务直接操作 entity 细节
 * @author goya
 * @since 2026/1/29 00:00
 */
public interface TenantProfileService extends IService<TenantProfileEntity> {

    /**
     * 根据租户 ID 获取租户画像（读模型）。
     *
     * @param tenantId 租户 ID
     * @return TenantProfile；不存在返回 null
     */
    TenantProfile getProfile(String tenantId);

    /**
     * 获取租户配置版本号。
     *
     * @param tenantId 租户 ID
     * @return version；不存在返回 0
     */
    long getVersion(String tenantId);
}
