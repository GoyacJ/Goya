package com.ysmjjsy.goya.component.mybatisplus.permission.store;

import com.ysmjjsy.goya.component.mybatisplus.permission.model.RuleSet;

/**
 * <p>权限规则存储接口</p>
 * <p>
 * 本模块只消费规则，不关心规则来自数据库、配置中心或远程服务。
 *
 * <p><b>缓存建议：</b>
 * <ul>
 *   <li>L1：请求内缓存（同一请求只编译一次）</li>
 *   <li>L2：Caffeine，key 包含 version</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:31
 */
public interface PermissionRuleStore {

    /**
     * 加载规则集。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @param resource 资源
     * @return 规则集；不存在返回 null
     */
    RuleSet load(String tenantId, String subjectId, String resource);

    /**
     * 获取规则版本。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @return 版本号
     */
    long version(String tenantId, String subjectId);
}