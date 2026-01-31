package com.ysmjjsy.goya.component.mybatisplus.context;

import lombok.experimental.UtilityClass;

/**
 * <p>租户上下文（线程级）</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@UtilityClass
public class TenantContext {

    private static final ThreadLocal<TenantContextValue> CONTEXT = new ThreadLocal<>();

    /**
     * 设置租户上下文。
     *
     * @param value 上下文值
     */
    public void set(TenantContextValue value) {
        CONTEXT.set(value);
    }

    /**
     * 获取租户上下文。
     *
     * @return 上下文值
     */
    public TenantContextValue get() {
        TenantContextValue value = CONTEXT.get();
        return value == null ? TenantContextValue.empty() : value;
    }

    /**
     * 获取租户 ID。
     *
     * @return tenantId
     */
    public String getTenantId() {
        return get().tenantId();
    }

    /**
     * 清理租户上下文。
     */
    public void clear() {
        CONTEXT.remove();
    }
}
