package com.ysmjjsy.goya.component.mybatisplus.context;

import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;

/**
 * <p>当前线程租户上下文值对象</p>
 * <p>
 * 该对象在一次请求/调用链内应保持稳定，不允许被业务代码随意修改。
 * 仅允许在“注入点”（Filter/Aspect）处创建并写入。
 *
 * <p><b>字段语义：</b>
 * <ul>
 *   <li>tenantId：租户唯一标识</li>
 *   <li>mode：租户落库模式</li>
 *   <li>dsKey：dynamic-datasource 使用的数据源键</li>
 * </ul>
 *
 * @param tenantId 租户 ID
 * @param mode     租户模式
 * @param dsKey    数据源路由键
 *
 * @author goya
 * @since 2026/1/28 21:48
 */
public record TenantContextValue(String tenantId, TenantMode mode, String dsKey) {

}
