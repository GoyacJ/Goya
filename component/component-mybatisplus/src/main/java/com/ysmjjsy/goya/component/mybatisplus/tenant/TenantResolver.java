package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户解析器</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public interface TenantResolver {

    /**
     * 解析租户 ID。
     *
     * @return tenantId
     */
    String resolveTenantId();
}
