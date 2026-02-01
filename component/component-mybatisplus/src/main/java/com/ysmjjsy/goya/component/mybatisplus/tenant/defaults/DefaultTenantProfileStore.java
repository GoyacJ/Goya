package com.ysmjjsy.goya.component.mybatisplus.tenant.defaults;

import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileStore;
import com.ysmjjsy.goya.component.mybatisplus.tenant.entity.TenantProfileEntity;
import com.ysmjjsy.goya.component.mybatisplus.tenant.mapper.TenantProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * <p>默认租户配置存储</p>
 *
 * <p>基于 tenant_profile 表读取租户配置。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@RequiredArgsConstructor
public class DefaultTenantProfileStore implements TenantProfileStore {

    private final TenantProfileMapper mapper;

    /**
     * 加载租户配置。
     *
     * @param tenantId 租户 ID
     * @return 租户配置
     */
    @Override
    public TenantProfile load(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            return null;
        }
        TenantProfileEntity entity = mapper.selectById(tenantId);
        if (entity == null) {
            return null;
        }
        TenantMode mode = entity.getMode();
        long version = entity.getTenantVersion() == null ? 0L : entity.getTenantVersion();
        boolean tenantLineEnabled = entity.getTenantLineEnabled() == null || entity.getTenantLineEnabled();
        TenantDataSourceProfile dataSourceProfile = resolveDataSourceProfile(entity);
        return new TenantProfile(entity.getTenantId(), mode, entity.getDsKey(), dataSourceProfile, tenantLineEnabled, version);
    }

    /**
     * 获取版本号。
     *
     * @param tenantId 租户 ID
     * @return 版本号
     */
    @Override
    public long version(String tenantId) {
        if (!StringUtils.hasText(tenantId)) {
            return 0L;
        }
        TenantProfileEntity entity = mapper.selectById(tenantId);
        return entity == null || entity.getTenantVersion() == null ? 0L : entity.getTenantVersion();
    }

    private TenantDataSourceProfile resolveDataSourceProfile(TenantProfileEntity entity) {
        if (entity == null || !StringUtils.hasText(entity.getJdbcUrl())) {
            return null;
        }
        return new TenantDataSourceProfile(
                entity.getJdbcUrl(),
                entity.getJdbcUsername(),
                entity.getJdbcPassword(),
                entity.getJdbcDriver(),
                entity.getDsType()
        );
    }
}
