package com.ysmjjsy.goya.component.mybatisplus.tenant.aspect;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContextValue;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantDataSourceRouter;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantMode;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfile;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileStore;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantResolver;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantShardDecider;
import com.ysmjjsy.goya.component.mybatisplus.tenant.annotation.TenantRouting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.util.StringUtils;

/**
 * <p>租户路由切面</p>
 *
 * <p>用于非 Web 场景的方法级路由。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class TenantRoutingAspect {

    private final TenantResolver tenantResolver;
    private final TenantProfileStore profileStore;
    private final TenantShardDecider shardDecider;
    private final TenantDataSourceRouter router;
    private final GoyaMybatisPlusProperties.Tenant options;

    /**
     * 执行租户路由。
     *
     * @param joinPoint 切点
     * @return 结果
     * @throws Throwable 异常
     */
    @Around("@annotation(tenantRouting) || @within(tenantRouting)")
    public Object route(ProceedingJoinPoint joinPoint, TenantRouting tenantRouting) throws Throwable {
        String tenantId = tenantResolver.resolveTenantId();
        if (!StringUtils.hasText(tenantId)) {
            if (options.requireTenant()) {
                throw new IllegalStateException("TenantId is required but missing.");
            }
            return joinPoint.proceed();
        }

        TenantProfile profile = profileStore.load(tenantId);
        TenantMode mode = profile == null ? shardDecider.decide(tenantId) : profile.mode();
        String dsKey = profile == null ? router.route(tenantId, mode) : profile.dsKey();
        boolean tenantLineEnabled = profile == null || profile.tenantLineEnabled();

        TenantContext.set(new TenantContextValue(tenantId, mode, dsKey, tenantLineEnabled));
        if (StringUtils.hasText(dsKey)) {
            DynamicDataSourceContextHolder.push(dsKey);
        }

        try {
            return joinPoint.proceed();
        } finally {
            DynamicDataSourceContextHolder.poll();
            TenantContext.clear();
        }
    }
}
