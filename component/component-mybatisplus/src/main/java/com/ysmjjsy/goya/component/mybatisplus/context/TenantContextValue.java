package com.ysmjjsy.goya.component.mybatisplus.context;

import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;

import java.io.Serial;
import java.io.Serializable;

/**
 * <p>租户上下文值对象</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public record TenantContextValue(
        String tenantId,
        TenantMode mode,
        String dsKey,
        boolean tenantLineEnabled
) implements Serializable {

    @Serial
    private static final long serialVersionUID = -2894069413078301058L;

    /**
     * 空上下文。
     *
     * @return 空对象
     */
    public static TenantContextValue empty() {
        return new TenantContextValue(null, null, null, true);
    }
}
