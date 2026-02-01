package com.ysmjjsy.goya.component.mybatisplus.tenant.defaults;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceRegistrar;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>默认租户数据源注册器。</p>
 *
 * <p>基于 DynamicRoutingDataSource 动态注册数据源。</p>
 *
 * @author goya
 * @since 2026/1/31 12:30
 */
@RequiredArgsConstructor
public class DefaultTenantDataSourceRegistrar implements TenantDataSourceRegistrar {

    private static final String DEDICATED_PREFIX = "tenant_";

    private final DynamicRoutingDataSource routingDataSource;
    private final Map<String, DataSource> cache = new ConcurrentHashMap<>();

    /**
     * 注册数据源并返回 dsKey。
     *
     * @param tenantId 租户 ID
     * @param profile 数据源配置
     * @param dsKeyHint dsKey 提示（可为空）
     * @return 数据源 key
     */
    @Override
    public String register(String tenantId, TenantDataSourceProfile profile, String dsKeyHint) {
        if (profile == null || !StringUtils.hasText(profile.jdbcUrl())) {
            return null;
        }
        String dsKey = resolveKey(tenantId, dsKeyHint);
        cache.computeIfAbsent(dsKey, key -> createDataSource(profile));
        routingDataSource.addDataSource(dsKey, cache.get(dsKey));
        return dsKey;
    }

    private String resolveKey(String tenantId, String dsKeyHint) {
        if (StringUtils.hasText(dsKeyHint)) {
            return dsKeyHint;
        }
        if (StringUtils.hasText(tenantId)) {
            return DEDICATED_PREFIX + tenantId;
        }
        return DEDICATED_PREFIX + "unknown";
    }

    private DataSource createDataSource(TenantDataSourceProfile profile) {
        DataSourceBuilder<?> builder = DataSourceBuilder.create();
        String driverClassName = profile.driverClassName();
        if (!StringUtils.hasText(driverClassName) && profile.type() != null) {
            driverClassName = profile.type().getDriverClassName();
        }
        if (StringUtils.hasText(driverClassName)) {
            builder.driverClassName(driverClassName);
        }
        builder.url(profile.jdbcUrl());
        builder.username(profile.username());
        builder.password(profile.password());
        return builder.build();
    }
}
