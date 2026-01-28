package com.ysmjjsy.goya.component.mybatisplus.tenant.profile;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileRepository;
import org.springframework.stereotype.Service;

/**
 * <p>租户画像配置服务实现</p>
 *
 * @author goya
 * @since 2026/1/29 00:00
 */
@Service
public class TenantProfileServiceImpl extends ServiceImpl<TenantProfileMapper, TenantProfileEntity>
        implements TenantProfileService, TenantProfileRepository {

    @Override
    public TenantProfile getProfile(String tenantId) {
        TenantProfileEntity e = getById(tenantId);
        if (e == null) {
            return null;
        }
        return new TenantProfile(
                e.getMode(),
                e.getDsKey(),
                e.getTenantLineEnabled() == null || e.getTenantLineEnabled()
        );
    }

    @Override
    public long getVersion(String tenantId) {
        LambdaQueryWrapper<TenantProfileEntity> qw = new LambdaQueryWrapper<>();
        qw.select(TenantProfileEntity::getVersion).eq(TenantProfileEntity::getTenantId, tenantId);

        TenantProfileEntity e = getOne(qw, false);
        if (e == null || e.getVersion() == null) {
            return 0L;
        }
        return e.getVersion();
    }

    @Override
    public TenantProfile findProfile(String tenantId) {
        return getProfile(tenantId);
    }

    @Override
    public long findVersion(String tenantId) {
        return getVersion(tenantId);
    }
}