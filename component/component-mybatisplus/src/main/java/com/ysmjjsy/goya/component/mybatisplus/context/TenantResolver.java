package com.ysmjjsy.goya.component.mybatisplus.context;

/**
 * <p>租户解析器</p>
 * <p>
 * 用于在请求/调用链开始阶段解析 tenantId。
 *
 * <p><b>默认策略（可选 Web 集成）：</b>
 * <ul>
 *   <li>优先读取已设置的 TenantContext（若上游已注入）</li>
 *   <li>若 classpath 存在 Spring Web，则从 Header（默认 X-Tenant-Id）读取</li>
 * </ul>
 *
 * <p><b>约束：</b>
 * tenantId 必须在事务开始前可用；缺失时是否拒绝由 requireTenant 开关控制。
 * @author goya
 * @since 2026/1/28 21:56
 */
public interface TenantResolver {

    /**
     * 解析当前租户 ID。
     *
     * @return 租户 ID；若无法解析则返回 null
     */
    String resolveTenantId();
}