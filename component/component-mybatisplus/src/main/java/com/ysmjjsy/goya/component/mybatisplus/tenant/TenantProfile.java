package com.ysmjjsy.goya.component.mybatisplus.tenant;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>租户配置</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public record TenantProfile(
        String tenantId,
        TenantMode mode,
        String dsKey,
        boolean tenantLineEnabled,
        long version
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 7468114250710852414L;
}
