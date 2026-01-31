package com.ysmjjsy.goya.component.mybatisplus.tenant;

/**
 * <p>租户模式</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public enum TenantMode {

    /**
     * 核心表共享
     */
    CORE_SHARED,

    /**
     * 独立库
     */
    DEDICATED_DB
}
