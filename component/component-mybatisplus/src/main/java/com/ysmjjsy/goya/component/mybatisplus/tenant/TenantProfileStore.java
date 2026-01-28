package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户画像存储（动态）</p>
 * 用于从数据库/配置中心等加载租户 profile，并提供版本用于缓存一致性校验。
 *
 * <p><b>缓存策略建议：</b>
 * <ul>
 *   <li>L2（Caffeine）：tenantId → TenantProfile（TTL + version 校验）</li>
 *   <li>version 变化时立即重载</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 21:50
 */
public interface TenantProfileStore {

    /**
     * 加载租户画像。
     *
     * @param tenantId 租户 ID
     * @return 租户画像
     */
    TenantProfile load(String tenantId);

    /**
     * 获取租户画像版本号（或更新时间戳）。
     * <p>
     * 用于缓存一致性判定；版本变化必须触发重新加载。
     *
     * @param tenantId 租户 ID
     * @return 版本号
     */
    long version(String tenantId);
}