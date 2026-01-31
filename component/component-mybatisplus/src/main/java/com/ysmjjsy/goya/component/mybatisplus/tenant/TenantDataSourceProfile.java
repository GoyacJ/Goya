package com.ysmjjsy.goya.component.mybatisplus.tenant;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>租户数据源配置。</p>
 *
 * @author goya
 * @since 2026/1/31 12:30
 */
public record TenantDataSourceProfile(
        String jdbcUrl,
        String username,
        String password,
        String driverClassName,
        TenantDataSourceType type
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 4066133291225363926L;
}
