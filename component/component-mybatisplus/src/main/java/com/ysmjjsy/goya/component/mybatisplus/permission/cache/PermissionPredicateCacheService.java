package com.ysmjjsy.goya.component.mybatisplus.permission.cache;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.CompiledPredicate;
import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.PermissionCompiler;
import com.ysmjjsy.goya.component.mybatisplus.permission.model.RuleSet;
import com.ysmjjsy.goya.component.mybatisplus.permission.resource.ResourceRegistry;
import com.ysmjjsy.goya.component.mybatisplus.permission.store.PermissionRuleStore;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * <p>权限谓词缓存服务</p>
 * <p>
 * 负责实现契约定义的缓存策略：
 * <ul>
 *   <li>L1（请求内）：(tenantId, subjectId, resource) -> CompiledPredicate</li>
 *   <li>L2（Caffeine）：(tenantId, subjectId, resource, version) -> CompiledPredicate</li>
 * </ul>
 *
 * <p><b>版本策略：</b>
 * version 来自 {@link PermissionRuleStore#version(String, String)}。
 * 当 version 变化时，L2 key 自然变化，从而实现“快速生效”（旧 key 过期淘汰）。
 *
 * <p><b>重要约束：</b>
 * L1 必须在请求结束清理：{@link PermissionRequestCache#clear()}。
 *
 * @author goya
 * @since 2026/1/28 22:56
 */
@Slf4j
@RequiredArgsConstructor
public class PermissionPredicateCacheService {

    /**
     * L2 缓存命名空间：已编译谓词。
     */
    public static final String CACHE_COMPILED_PREDICATE = "goya:mybatis-plus:permission:compiled";

    private final CacheService cacheService;
    private final PermissionRuleStore ruleStore;
    private final PermissionCompiler compiler;
    private final ResourceRegistry registry;

    /**
     * L2 TTL（可选）。
     * <p>
     * 即使 TTL 较长也不会影响“快速生效”，因为 key 包含 version。
     * TTL 的主要作用是控制内存/存储占用，淘汰旧版本 key。
     */
    @Getter
    private final Duration l2Ttl;

    /**
     * 获取或编译权限谓词（带 L1/L2 缓存）。
     *
     * <p><b>返回值语义：</b>
     * <ul>
     *   <li>返回非 null：成功获取编译结果，可用于生成 where 条件</li>
     *   <li>返回 null：规则不存在或为空（是否 failClosed 由上层决定）</li>
     * </ul>
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @param resource 资源
     * @param access 访问上下文（变量解析）
     * @return 已编译谓词；无规则返回 null
     */
    public CompiledPredicate getOrCompile(String tenantId,
                                          String subjectId,
                                          String resource,
                                          AccessContextValue access) {

        // 1) L1（请求内）命中：同一请求只编译一次
        CompiledPredicate l1 = PermissionRequestCache.getPredicate(tenantId, subjectId, resource);
        if (l1 != null) {
            return l1;
        }

        // 2) version 请求内 L1：同一请求内只查一次
        Long cachedVersion = PermissionRequestCache.getVersion(tenantId, subjectId);
        long version = cachedVersion != null ? cachedVersion : ruleStore.version(tenantId, subjectId);
        if (cachedVersion == null) {
            PermissionRequestCache.putVersion(tenantId, subjectId, version);
        }

        // 3) L2 key：必须包含 version
        PermissionPredicateCacheKey key = new PermissionPredicateCacheKey(tenantId, subjectId, resource, version);

        // 4) L2 getOrLoad（并发收敛）
        CompiledPredicate compiled = cacheService.getOrLoad(
                CACHE_COMPILED_PREDICATE,
                key,
                CompiledPredicate.class,
                () -> {
                    // 注意：loader 内部不要写入 L1（避免重复）；只负责“计算值”
                    RuleSet ruleSet = ruleStore.load(tenantId, subjectId, resource);
                    if (ruleSet == null || ruleSet.getRules() == null || ruleSet.getRules().isEmpty()) {
                        // 空规则：返回 null，且不应写入缓存（但 getOrLoad 语义通常会缓存 null？）
                        // 由于不同 CacheService 对 null 处理不同，这里用特殊约定：
                        // 1) 返回 null 表示不缓存，由 CacheService 实现保证（推荐）
                        // 2) 若你的 CacheService 会缓存 null，请改为抛出受控异常并在外层转为 null（见下方注释）
                        return null;
                    }
                    return compiler.compile(ruleSet, access, registry);
                }
        );

        // 5) 若 CacheService 对 null 会缓存（实现差异），你需要选择一种策略：
        // - 推荐：CacheService 约定 loader 返回 null 不写入缓存
        // - 否则：改为 loader 抛出 NoRuleException，然后这里 catch 后 return null（并不写入 L2）

        // 6) 写入 L1（请求内）
        if (compiled != null) {
            PermissionRequestCache.putPredicate(tenantId, subjectId, resource, compiled);
        }
        return compiled;
    }
}
