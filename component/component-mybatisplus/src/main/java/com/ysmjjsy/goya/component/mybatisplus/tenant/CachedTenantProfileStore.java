package com.ysmjjsy.goya.component.mybatisplus.tenant;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;

import java.io.Serializable;
import java.time.Duration;
import java.util.Objects;

/**
 * <p>带 L2 缓存与版本校验的 TenantProfileStore 实现</p>
 * <p>
 * 实现契约：
 * <ul>
 *   <li>L2：tenantId -> TenantProfile（带 TTL）</li>
 *   <li>version 变化立即生效：通过 key 包含 version 实现</li>
 * </ul>
 *
 * <h2>缓存设计</h2>
 * <ul>
 *   <li>版本缓存：tenantId -> version</li>
 *   <li>画像缓存：(tenantId, version) -> TenantProfile</li>
 * </ul>
 *
 * <p><b>为何不直接 tenantId -> profile？</b>
 * 因为那样只能靠 TTL 等待生效；我们要“版本变化立即生效”。
 *
 * <h2>空值策略</h2>
 * 默认不缓存 null（避免空缓存导致刚配置租户却被 TTL 卡住）。
 * 若你的 CacheService 会缓存 null，请参考 Permission 的策略用受控异常阻止写入。
 * @author goya
 * @since 2026/1/28 23:44
 */
public class CachedTenantProfileStore implements TenantProfileStore {

    public static final String CACHE_TENANT_VERSION = "goya:mybatis-plus:tenant:version";
    public static final String CACHE_TENANT_PROFILE = "goya:mybatis-plus:tenant:profile";

    private final CacheService cacheService;
    private final TenantProfileRepository repository;

    private final Duration versionTtl;
    private final Duration profileTtl;

    public CachedTenantProfileStore(CacheService cacheService,
                                    TenantProfileRepository repository,
                                    Duration versionTtl,
                                    Duration profileTtl) {
        this.cacheService = Objects.requireNonNull(cacheService, "cacheService 不能为空");
        this.repository = Objects.requireNonNull(repository, "repository 不能为空");
        this.versionTtl = versionTtl;
        this.profileTtl = profileTtl;
    }

    @Override
    public TenantProfile load(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return null;
        }

        long ver = version(tenantId);
        if (ver <= 0) {
            // 无版本：视为不存在配置
            return null;
        }

        ProfileKey key = new ProfileKey(tenantId, ver);

        // getOrLoad：并发收敛
        return (profileTtl == null || profileTtl.isZero() || profileTtl.isNegative())
                ? cacheService.getOrLoad(CACHE_TENANT_PROFILE, key, TenantProfile.class,
                () -> repository.findProfile(tenantId))
                : cacheService.getOrLoad(CACHE_TENANT_PROFILE, key, TenantProfile.class, profileTtl,
                () -> repository.findProfile(tenantId));
    }

    @Override
    public long version(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            return 0L;
        }

        Long v = (versionTtl == null || versionTtl.isZero() || versionTtl.isNegative())
                ? cacheService.getOrLoad(CACHE_TENANT_VERSION, tenantId, Long.class,
                () -> repository.findVersion(tenantId))
                : cacheService.getOrLoad(CACHE_TENANT_VERSION, tenantId, Long.class, versionTtl,
                () -> repository.findVersion(tenantId));

        return v == null ? 0L : v;
    }

    /**
     * TenantProfile 缓存 key：包含 version。
     *
     * @param tenantId 租户 ID
     * @param version 版本号
     */
    public record ProfileKey(String tenantId, long version) implements Serializable {
    }
}
