package com.ysmjjsy.goya.component.mybatisplus.context;

/**
 * <p>租户上下文（线程级）</p>
 * 统一管理当前线程的租户信息与数据源路由结果，避免在系统中散落多个 ThreadLocal。
 *
 * <p><b>使用约束：</b>
 * <ul>
 *   <li>任何数据库操作前必须存在 TenantContext（生产默认 requireTenant=true）</li>
 *   <li>dsKey 必须在事务开始前确定并写入 dynamic-datasource 上下文</li>
 *   <li>请求结束必须在 finally 清理，防止线程复用污染</li>
 * </ul>
 *
 * <p><b>禁止：</b>
 * <ul>
 *   <li>业务层随意 set() / clear()</li>
 *   <li>在 Mapper/Service 内部临时篡改租户</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 21:55
 */
public final class TenantContext {

    private static final ThreadLocal<TenantContextValue> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    /**
     * 写入当前线程租户上下文。
     *
     * @param value 租户上下文值
     */
    public static void set(TenantContextValue value) {
        HOLDER.set(value);
    }

    /**
     * 获取当前线程租户上下文。
     *
     * @return 租户上下文值；若未设置则返回 null
     */
    public static TenantContextValue get() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程租户上下文。
     * <p>
     * 必须在 finally 调用，防止线程复用时串租户。
     */
    public static void clear() {
        HOLDER.remove();
    }
}