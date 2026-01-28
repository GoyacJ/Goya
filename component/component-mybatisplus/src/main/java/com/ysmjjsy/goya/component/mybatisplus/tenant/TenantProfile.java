package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户画像</p>
 * 作为租户存储形态与路由结果的事实源（Source of Truth）。
 *
 * <p><b>字段语义：</b>
 * <ul>
 *   <li>mode：租户落库模式</li>
 *   <li>dsKey：dynamic-datasource 数据源键</li>
 *   <li>tenantLineEnabled：是否启用租户列隔离（独库也可能需要）</li>
 * </ul>
 * @author goya
 * @since 2026/1/28 21:51
 */
public record TenantProfile(TenantMode mode, String dsKey, boolean tenantLineEnabled) {

}
