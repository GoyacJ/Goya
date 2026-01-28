package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户画像仓储（真实数据源读取）</p>
 * <p>
 * 业务侧实现该接口从数据库/配置中心读取租户画像与版本。
 *
 * <p><b>建议：</b>
 * <ul>
 *   <li>version 使用自增版本或 updated_at 转 long（毫秒）</li>
 *   <li>load 与 version 必须走索引/高效查询</li>
 * </ul>
 * @author goya
 * @since 2026/1/28 23:43
 */
public interface TenantProfileRepository {

    /**
     * 查询租户画像。
     *
     * @param tenantId 租户 ID
     * @return TenantProfile；不存在返回 null
     */
    TenantProfile findProfile(String tenantId);

    /**
     * 查询租户配置版本号。
     *
     * @param tenantId 租户 ID
     * @return version；不存在返回 0
     */
    long findVersion(String tenantId);
}