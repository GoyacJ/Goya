package com.ysmjjsy.goya.component.mybatisplus.permission.cache;

import com.ysmjjsy.goya.component.mybatisplus.permission.compiler.CompiledPredicate;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>权限请求内缓存</p>
 * <p>
 * 用于确保同一请求/调用链中：
 * <ul>
 *   <li>同一 (tenantId, subjectId, resource) 的规则只加载/编译一次</li>
 *   <li>同一 (tenantId, subjectId) 的 version 只查询一次</li>
 * </ul>
 *
 * <p><b>实现说明：</b>
 * 使用 ThreadLocal 存储 Map。
 * <p>
 * <b>重要约束：</b>
 * 必须在请求结束（finally）清理 {@link #clear()}，否则线程复用会串缓存。
 *
 * @author goya
 * @since 2026/1/28 22:54
 */
public class PermissionRequestCache {
    private static final ThreadLocal<Map<RequestKey, CompiledPredicate>> PREDICATE_CACHE = new ThreadLocal<>();
    private static final ThreadLocal<Map<VersionKey, Long>> VERSION_CACHE = new ThreadLocal<>();

    private PermissionRequestCache() {
    }

    /**
     * 获取请求内已编译谓词缓存命中。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @param resource 资源
     * @return 已编译谓词；若未命中返回 null
     */
    public static CompiledPredicate getPredicate(String tenantId, String subjectId, String resource) {
        Map<RequestKey, CompiledPredicate> map = PREDICATE_CACHE.get();
        if (map == null) {
            return null;
        }
        return map.get(new RequestKey(tenantId, subjectId, resource));
    }

    /**
     * 写入请求内已编译谓词缓存。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @param resource 资源
     * @param predicate 已编译谓词
     */
    public static void putPredicate(String tenantId, String subjectId, String resource, CompiledPredicate predicate) {
        Objects.requireNonNull(predicate, "predicate 不能为空");
        Map<RequestKey, CompiledPredicate> map = PREDICATE_CACHE.get();
        if (map == null) {
            map = new HashMap<>();
            PREDICATE_CACHE.set(map);
        }
        map.put(new RequestKey(tenantId, subjectId, resource), predicate);
    }

    /**
     * 获取请求内版本缓存命中。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @return version；若未命中返回 null
     */
    public static Long getVersion(String tenantId, String subjectId) {
        Map<VersionKey, Long> map = VERSION_CACHE.get();
        if (map == null) {
            return null;
        }
        return map.get(new VersionKey(tenantId, subjectId));
    }

    /**
     * 写入请求内版本缓存。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @param version 版本号
     */
    public static void putVersion(String tenantId, String subjectId, long version) {
        Map<VersionKey, Long> map = VERSION_CACHE.get();
        if (map == null) {
            map = new HashMap<>();
            VERSION_CACHE.set(map);
        }
        map.put(new VersionKey(tenantId, subjectId), version);
    }

    /**
     * 清理请求内缓存。
     * <p>
     * 必须在 finally 调用，避免线程复用污染。
     */
    public static void clear() {
        PREDICATE_CACHE.remove();
        VERSION_CACHE.remove();
    }

    /**
     * 请求内谓词缓存 key。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     * @param resource 资源
     */
    public record RequestKey(String tenantId, String subjectId, String resource) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof RequestKey(String id, String subjectId1, String resource1))) {
                return false;
            }
            return Objects.equals(tenantId(), id) && Objects.equals(resource(), resource1) && Objects.equals(subjectId(), subjectId1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId(), subjectId(), resource());
        }
    }

    /**
     * 请求内版本缓存 key。
     *
     * @param tenantId 租户 ID
     * @param subjectId 主体 ID
     */
    public record VersionKey(String tenantId, String subjectId) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof VersionKey(String id, String subjectId1))) {
                return false;
            }
            return Objects.equals(tenantId(), id) && Objects.equals(subjectId(), subjectId1);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tenantId(), subjectId());
        }
    }
}